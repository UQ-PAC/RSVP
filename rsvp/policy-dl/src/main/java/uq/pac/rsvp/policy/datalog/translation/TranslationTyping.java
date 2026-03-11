package uq.pac.rsvp.policy.datalog.translation;

import com.google.common.collect.HashMultimap;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;
import static uq.pac.rsvp.policy.datalog.translation.TranslationError.error;
import static uq.pac.rsvp.policy.datalog.util.Util.required;

/**
 * Type inference during translation
 */
public class TranslationTyping {
    private final HashMultimap<VariableExpression.Reference, String> typing;
    private final Schema schema;

    public TranslationTyping(Schema schema) {
        this.typing = HashMultimap.create();
        this.schema = schema;

        // Build initial typing information for variables based on actions
        schema.actionNames().forEach(a -> {
            typing.put(Action, a);
            ActionDefinition ad = schema.getAction(a);
            for (EntityTypeDefinition pt : ad.getAppliesToPrincipalTypes()) {
                typing.put(Principal, pt.getName());
            }
            for (EntityTypeDefinition rt : ad.getAppliesToResourceTypes()) {
                typing.put(Resource, rt.getName());
            }
        });

        // Stop if either of the variables have no associated types
        Stream.of(Principal, Resource, Action).forEach(v -> {
            error(typing.containsKey(v), "No available types for variable: " + v);
        });
    }

    // Retrieve type information in a safe way
    public Set<String> get(VariableExpression.Reference ref) {
        return switch (ref) {
            case Action, Principal, Resource -> typing.get(ref);
            case Context -> throw new TranslationError("Unsupported variable reference: " + ref);
        };
    }

    // Update type information for a given variable using the following semantics.
    // Consider set types to be [t1, t2]. Then
    //   (*) if negated is true, then we assume !(ref is t1) && !(ref is t2)
    //        that is, all input types are removed
    //   (*) if negated is false, then we assume ref is t1 || ref is t2
    //        that is, only input types are retained
    private void update(VariableExpression.Reference ref, boolean negated, Set<String> types) {
        for (String type : types) {
            switch (ref) {
                case Principal, Resource -> error(schema.getEntityType(type) != null,
                        "Cannot locate entity definition: " + type);
                case Action -> error(schema.getAction(type) != null,
                        "Cannot locate action definition: " + type);
                case Context -> error("Unsupported variable: " + ref.name());
            }
        }

        Set<String> applicable = typing.get(ref);
        if (negated) {
            applicable.removeAll(types);
        } else {
            applicable.removeIf(e -> !types.contains(e));
        }

        // If we are updating action, we also need to update principal and resource as per schema
        if (ref == Action) {
            Set<String> appliesToPrincipalTypes = applicable.stream().map(schema::getAction)
                    .flatMap(a -> a.getAppliesToPrincipalTypes().stream())
                    .map(EntityTypeDefinition::getName)
                    .collect(Collectors.toSet());

            Set<String> principalTypes = typing.get(Principal);
            for (String pt : appliesToPrincipalTypes) {
                if (!principalTypes.contains(pt)) {
                    principalTypes.remove(pt);
                }
            }

            Set<String> appliesToResourceTypes = applicable.stream().map(schema::getAction)
                    .flatMap(a -> a.getAppliesToResourceTypes().stream())
                    .map(EntityTypeDefinition::getName)
                    .collect(Collectors.toSet());

            Set<String> resourceTypes = typing.get(Resource);
            for (String pt : appliesToResourceTypes) {
                if (!resourceTypes.contains(pt)) {
                    resourceTypes.remove(pt);
                }
            }
        }
    }

    public void update(VariableExpression.Reference ref, boolean negated, String type) {
        update(ref, negated, Set.of(type));
    }

    public void update(VariableExpression.Reference ref, boolean negated, EntityExpression expr) {
        update(ref, negated, getTypeName(expr));
    }

    public static String getTypeName(CommonTypeDefinition def) {
        return switch (def) {
            case EntityTypeReference t -> t.getDefinition().getName();
            case CommonTypeReference t -> t.getDefinition().getName();
            case SetTypeDefinition t -> "Set<" + getTypeName(t.getElementType()) + ">";
            case LongType t -> "Long";
            case StringType t -> "String";
            case BooleanType t -> "Bool";
            default -> throw new TranslationError("Unsupported type: " + def);
        };
    }

    public static String getTypeName(Expression expr) {
        return expr.compute(new ValueVisitorAdapter<String>() {
            @Override
            public String visitEntityExpr(EntityExpression expr) {
                String qualifiedType = expr.getQualifiedType();
                return (qualifiedType.equals("Action") || qualifiedType.endsWith("::Action")) ?
                    expr.getQualifiedEid() : qualifiedType;
            }

            @Override
            public String visitStringExpr(StringExpression expr) {
                return getTypeName(new StringType());
            }

            @Override
            public String visitLongExpr(LongExpression expr) {
                return getTypeName(new LongType());
            }
        });
    }

    public void update(Expression expr, boolean negated) {
        expr.accept(new VoidVisitorAdapter() {
            @Override
            public void visitBinaryExpr(BinaryExpression expr) {
                Expression lhs = expr.getLeft(), rhs = expr.getRight();
                switch (expr.getOp()) {
					// FIXME: I can think of a use-case that restricts type information in general here
					//		  Need to include this type reduction
                    case LessEq, Less, Greater, GreaterEq, In -> {}
                    case Eq -> {
                        if (lhs instanceof VariableExpression v && rhs instanceof EntityExpression e) {
                            update(v.getReference(), negated, e);
                        } else if (rhs instanceof VariableExpression v && lhs instanceof EntityExpression e) {
                            update(v.getReference(), negated, e);
                        }
                    }
                    case Neq -> {
                        throw new TranslationError("Expected '!=' rewritten to '=='");
                    }
                    case Is -> {
                        TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                        if (lhs instanceof VariableExpression v) {
                            update(v.getReference(), negated, typeExpr.getValue());
                        }
                    }
                    default -> throw new RuntimeException("unsupported: " + expr.getOp());
                }
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (VariableExpression.Reference ref : typing.keySet()) {
            sb.append("   ")
                    .append(ref.getValue())
                    .append(": ")
                    .append(typing.get(ref))
                    .append('\n');
        }
        return sb.toString();
    }

}
