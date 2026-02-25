package uq.pac.rsvp.policy.datalog.translation;

import com.google.common.collect.HashMultimap;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;
import static uq.pac.rsvp.policy.datalog.translation.TranslationError.error;
import static uq.pac.rsvp.policy.datalog.util.Util.required;

public class TranslationTyping {
    private final HashMultimap<VariableExpression.Reference, String> typing;
    private final Schema schema;

    public TranslationTyping(Schema schema) {
        this.typing = HashMultimap.create();
        this.schema = schema;

        schema.entityTypeNames().forEach(e -> {
                typing.put(Principal, e);
                typing.put(Resource, e);
        });

        schema.actionNames().forEach(a -> {
                typing.put(Action, a);
        });

        Stream.of(Principal, Resource, Action).forEach(v -> {
            error(typing.containsKey(Principal), "No available types for: " + Principal);
        });
    }

    boolean supported(VariableExpression.Reference ref) {
        return switch (ref) {
            case Action, Principal, Resource -> true;
            case Context -> false;
        };
    }

    Set<String> get(VariableExpression.Reference ref) {
        error(supported(ref), "Unsupported variable reference: " + ref);
        return typing.get(ref);
    }

    public void update(VariableExpression.Reference ref, String ...types) {
        update(ref, Arrays.stream(types).collect(Collectors.toCollection(HashSet::new)));
    }

    public void update(VariableExpression.Reference ref, Set<String> types) {
        error(supported(ref), "Unsupported variable reference: " + ref);
        for (String type : types) {
            switch (ref) {
                case Principal, Resource ->
                        error(schema.getEntityType(type) != null, "Cannot locate entity definition: " + type);
                case Action ->
                        error(schema.getAction(type) != null, "Cannot locate entity definition: " + type);
                default ->
                        error("Unexpected variable: " + ref.name());
            }
        }

        Set<String> applicable = typing.get(ref);
        applicable.removeIf(e -> !types.contains(e));
        error(!applicable.isEmpty(), "No available types for: " + ref + " after reduction");

        // FIXME: If we are updating action, we also need to update principal and resource as per
        //        limits of the schema
    }

    // FIXME: May be not needed, check CommonTypeDefinition.getName
    public static String getTypeName(CommonTypeDefinition def) {
        return switch (def) {
            case EntityTypeReference t -> t.getDefinition().getName();
            case CommonTypeReference t -> t.getDefinition().getName();
            case SetTypeDefinition t -> "Set<" + getTypeName(t.getElementType()) + ">";
            case LongType t -> "Long";
            case StringType t -> "String";
            case BooleanType t -> "Bool";
            case IpAddressType t -> "ipaddr";
            case DurationType t -> "duration";
            case DecimalType t -> "decimal";
            case DateTimeType t -> "datetime";
            default -> throw new TranslationError("unsupported type: " + def);
        };
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

    public void update(Expression expr) {
        expr.accept(new VoidVisitorAdapter() {
            @Override
            public void visitBinaryExpr(BinaryExpression expr) {
                Expression lhs = expr.getLeft(),
                        rhs = expr.getRight();
                switch (expr.getOp()) {
                    case Eq -> {
                        if (lhs instanceof VariableExpression v && rhs instanceof EntityExpression e) {
                            update(v.getReference(), e.getUnquotedName());
                        }
                    }
                    case And -> {}
                    case Is -> {
                        TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                        if (lhs instanceof VariableExpression v) {
                            update(v.getReference(), typeExpr.getValue());
                        }
                    }
                    default -> throw new RuntimeException("unsupported: " + expr.getOp());
                }
            }
        });
    }
}
