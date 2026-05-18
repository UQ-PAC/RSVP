package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.support.error.TranslationError;
import uq.pac.rsvp.support.error.ValidationError;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.invariant.InvariantTyping.*;
import static uq.pac.rsvp.Assertion.require;

public class InvariantValidator implements PolicyComputationVisitor<BuiltinType> {
    // Overall custom-types including actions types and entity types
    private final Set<TypeReference> types;
    // Types of quantifier variables
    private final Map<String, TypeReference> variables;
    // Types of entities
    private final Schema schema;
    private final InvariantTyping typing;

    private InvariantValidator(InvariantValidator factory, Invariant invariant) {
        this.types = Set.copyOf(factory.types);
        this.variables = getVariables(invariant, types);
        this.typing = factory.typing;
        this.schema = factory.schema;
    }

    /**
     * Public constructor. Acts like a factory in that it builds internal structures,
     * such as available types, entities and actions. It deliberately leaves invariant-specific
     * structures (i.e., variables) nullified. Invariant validation is done via the private
     * constructor that copies general data from this factory object, computes invariant-specific
     * information and does validation.
     */
    public InvariantValidator(Schema schema) {
        this.types = new HashSet<>();
        this.variables = null;
        this.typing = new InvariantTyping(schema);
        this.schema = schema;

        // Build types from entities
        schema.entityTypes().forEach(e -> types.add(e.getTypeReference()));
        // Build types from actions
        schema.actions().forEach(e -> types.add(new TypeReference(e.getNamespace(), "Action")));
    }

    private static Map<String, TypeReference> getVariables(Invariant invariant, Set<TypeReference> types) {
        Map<String, TypeReference> variables = new HashMap<>();
        invariant.getQuantifier().getVariables().forEach(var -> {
            // Variable type
            TypeReference typeRef = TypeReference.parse(var.type().getValue());
            String varName = var.name().getReference();

            // Check for duplicate variables
            if (variables.containsKey(varName)) {
                throw new ValidationError(
                        "duplicate variable name: %s in quantifier: %s".formatted(varName, invariant.getQuantifier()));
            }

            // Ensure types exist
            if (!types.contains(typeRef)) {
                throw new ValidationError("invalid type: %s in quantifier: %s. Available types: %s".formatted(
                        var.type(), invariant.getQuantifier(), types));
            }

            variables.put(varName, typeRef);
        });
        return variables;
    }

    private BuiltinType collect(Expression expr) {
        return Objects.requireNonNull(expr.compute(this));
    }

    private List<BuiltinType> collect(Collection<Expression> exprs) {
        return exprs.stream().map(this::collect).toList();
    }

    public void validate(Invariant invariant) {
        require(this.variables == null);
        InvariantValidator validator = new InvariantValidator(this, invariant);
        expect(validator.collect(invariant.getExpression()), TBoolean);
    }

    @Override
    public BuiltinType visitBinaryExpr(BinaryExpression expr) {
        BuiltinType lhs = collect(expr.getLeft());
        BuiltinType rhs = collect(expr.getRight());

        return switch (expr.getOp()) {
            case Add, Sub, Mul -> {
                expectCompatible(lhs, rhs, TLong);
                yield LongType;
            }
            case Less, LessEq, Greater, GreaterEq -> {
                expectCompatible(lhs, rhs, TLong);
                yield BooleanType;
            }
            case Eq, Neq -> {
                // FIXME: we need record here as well
                expectCompatible(lhs, rhs, TBoolean, TLong, TString, TEntityOrAction);
                yield BooleanType;
            }
            case Or, And -> {
                expectCompatible(lhs, rhs, TBoolean);
                yield BooleanType;
            }
            case HasAttr -> {
                expect(lhs, TEntityOrAction, TRecord);
                expect(rhs, TString);
                yield BooleanType;
            }
            case Is -> {
                expect(lhs, TEntityOrAction);
                expect(rhs, TTypeOfEntity);
                yield BooleanType;
            }
            case In -> {
                expectCompatible(lhs, rhs, TEntityOrAction);
                yield BooleanType;
            }
            default -> throw new TranslationError("Unsupported");
        };
    }

    @Override
    public BuiltinType visitPropertyAccessExpr(PropertyAccessExpression expr) {
        BuiltinType objectType = collect(expr.getObject());
        if (objectType instanceof TypeReference ref) {
            objectType = switch (typing.getSchema().get(ref)) {
                case EntityTypeDefinition t -> t.getShape();
                case CommonTypeDefinition t -> t.getDefinition();
                default -> null;
            };
        }
        if (objectType instanceof RecordType rec) {
            BuiltinType attrType = rec.getAttribute(expr.getProperty());
            if (attrType != null) {
                return attrType;
            }
        }
        throw new ValidationError("Invalid property access: %s [%s: %s]"
                .formatted(expr, expr.getObject(), objectType.toString()));
    }

    @Override
    public BuiltinType visitUnaryExpr(UnaryExpression expr) {
        BuiltinType type = collect(expr.getExpression());
        return switch (expr.getOp()) {
            case Not -> {
                expect(type, TBoolean);
                yield BooleanType;
            }
            case Neg -> {
                expect(type, TLong);
                yield LongType;
            }
        };
    }

    @Override
    public BuiltinType visitBooleanExpr(BooleanExpression expr) {
        return BooleanType;
    }

    @Override
    public BuiltinType visitVariableExpr(VariableExpression expr) {
        String ref = expr.getReference();
        if (variables.containsKey(ref)) {
            return variables.get(ref);
        }
        throw new ValidationError("Ungrounded variable: " + ref);
    }

    @Override
    public BuiltinType visitLongExpr(LongExpression expr) {
        return LongType;
    }

    @Override
    public BuiltinType visitStringExpr(StringExpression expr) {
        return StringType;
    }

    @Override
    public BuiltinType visitEntityExpr(EntityExpression expr) {
        TypeReference ref = TypeReference.parse(expr.getType());

        EnumEntityTypeDefinition enumType = schema.getEnumEntityType(ref);
        // We do not check specific entities, but since enum
        // entities are specified by the schema we check if they exist
        if (enumType != null && !enumType.getEnumNames().contains(expr.getName())) {
            throw new ValidationError("invalid enum entity reference: " + expr.getQualifiedName());
        }

        if (types.contains(ref)) {
            return ref;
        }
        throw new ValidationError("invalid type reference: " + expr.getQualifiedName());
    }

    @Override
    public BuiltinType visitActionExpr(ActionExpression expr) {
        TypeReference action = TypeReference.parse(expr.getQualifiedName());
        if (schema.getAction(action) == null) {
            throw new ValidationError("invalid action: " + action);
        }
        TypeReference ref = TypeReference.parse(expr.getType());
        if (types.contains(ref)) {
            return ref;
        }
        throw new ValidationError("invalid type reference: " + ref);
    }

    @Override
    public BuiltinType visitTypeExpr(TypeExpression expr) {
        TypeReference ref = TypeReference.parse(expr.getValue());
        if (types.contains(ref)) {
            return TypeOfEntityType;
        }
        throw new ValidationError("invalid type: %s in type expression: %s. Available types: %s"
                .formatted(expr.getValue(), expr, types));
    }

    @Override
    public BuiltinType visitCallExpr(CallExpression expr) {
        String name = expr.getFunc();
        InvariantFunctionValidator.FunctionValidator validator =
                InvariantFunctionValidator.getValidator(name);
        if (validator == null) {
            throw new ValidationError("Function: " + name + " not registered");
        }
        BuiltinType self = expr.getSelf() == null ? null : collect(expr.getSelf());
        return validator.validate(self, collect(expr.getArgs()));
    }

    // == Unsupported
    @Override
    public BuiltinType visitConditionalExpr(ConditionalExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public BuiltinType visitSetExpr(SetExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public BuiltinType visitPolicy(Policy policy) {
        throw new TranslationError("unsupported element: " + policy);
    }

    @Override
    public BuiltinType visitInvariant(Invariant invariant) {
        throw new TranslationError("unsupported element: " + invariant);
    }

    @Override
    public BuiltinType visitQuantifier(Quantifier quantifier) {
        throw new TranslationError("unsupported element: " + quantifier);
    }

    @Override
    public BuiltinType visitRecordExpr(RecordExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }
}
