package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.invariant.Quantifier;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.invariant.InvariantTyping.*;
import static uq.pac.rsvp.Assertion.require;

public class InvariantValidator implements PolicyComputationVisitor<AntlrBuiltinType> {
    // Overall custom-types including actions types and entity types
    private final Set<AntlrTypeReference> types;
    // Types of quantifier variables
    private final Map<String, AntlrTypeReference> variables;
    // Types of entities
    private final AntlrSchema schema;
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
    public InvariantValidator(AntlrSchema schema) {
        this.types = new HashSet<>();
        this.variables = null;
        this.typing = new InvariantTyping(schema);
        this.schema = schema;

        // Build types from entities
        schema.entityTypes().forEach(e -> types.add(e.getTypeReference()));
        // Build types from actions
        schema.actions().forEach(e -> types.add(new AntlrTypeReference(e.getNamespace(), "Action")));
    }

    private static Map<String, AntlrTypeReference> getVariables(Invariant invariant, Set<AntlrTypeReference> types) {
        Map<String, AntlrTypeReference> variables = new HashMap<>();
        invariant.getQuantifier().getVariables().forEach(var -> {
            // Variable type
            AntlrTypeReference typeRef = AntlrTypeReference.parse(var.type().getValue());
            String varName = var.name().getReference();

            // Check for duplicate variables
            if (variables.containsKey(varName)) {
                throw new Error("duplicate variable name: %s in quantifier: %s", varName, invariant.getQuantifier());
            }

            // FIXME: Action types are not handled here
            // Ensure types exist
            if (!types.contains(typeRef)) {
                throw new Error("invalid type: %s in quantifier: %s. Available types: %s",
                        var.type(), invariant.getQuantifier(), types);
            }

            variables.put(varName, typeRef);
        });
        return variables;
    }

    static class Error extends RuntimeException {
        public Error(Object format, Object ...args) {
            super("Invariant validation error: " + String.format(format.toString(), args));
        }
    }

    private AntlrBuiltinType collect(Expression expr) {
        return Objects.requireNonNull(expr.compute(this));
    }

    private List<AntlrBuiltinType> collect(Collection<Expression> exprs) {
        return exprs.stream().map(this::collect).toList();
    }

    public void validate(Invariant invariant) {
        require(this.variables == null);
        InvariantValidator validator = new InvariantValidator(this, invariant);
        expect(validator.collect(invariant.getExpression()), TBoolean);
    }

    @Override
    public AntlrBuiltinType visitBinaryExpr(BinaryExpression expr) {
        AntlrBuiltinType lhs = collect(expr.getLeft());
        AntlrBuiltinType rhs = collect(expr.getRight());

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
    public AntlrBuiltinType visitPropertyAccessExpr(PropertyAccessExpression expr) {
        AntlrBuiltinType objectType = collect(expr.getObject());
        if (objectType instanceof AntlrTypeReference ref) {
            objectType = switch (typing.getSchema().get(ref)) {
                case AntlrEntityType t -> t.getShape();
                case AntlrCommonType t -> t.getDefinition();
                default -> null;
            };
        }
        if (objectType instanceof AntlrRecordType rec) {
            AntlrBuiltinType attrType = rec.getAttribute(expr.getProperty());
            if (attrType != null) {
                return attrType;
            }
        }
        throw new Error("Invalid property access: %s [%s: %s]", expr, expr.getObject(), objectType.toString());
    }

    @Override
    public AntlrBuiltinType visitUnaryExpr(UnaryExpression expr) {
        AntlrBuiltinType type = collect(expr.getExpression());
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
    public AntlrBuiltinType visitBooleanExpr(BooleanExpression expr) {
        return BooleanType;
    }

    @Override
    public AntlrBuiltinType visitVariableExpr(VariableExpression expr) {
        String ref = expr.getReference();
        if (variables.containsKey(ref)) {
            return variables.get(ref);
        }
        throw new Error("Ungrounded variable: %s", ref);
    }

    @Override
    public AntlrBuiltinType visitLongExpr(LongExpression expr) {
        return LongType;
    }

    @Override
    public AntlrBuiltinType visitStringExpr(StringExpression expr) {
        return StringType;
    }

    @Override
    public AntlrBuiltinType visitEntityExpr(EntityExpression expr) {
        AntlrTypeReference ref = AntlrTypeReference.parse(expr.getType());

        AntlrEnumEntityType enumType = schema.getEnumEntityType(ref);
        // We do not check specific entities, but since enum
        // entities are specified by the schema we check if they exist
        if (enumType != null && !enumType.getEnumNames().contains(expr.getName())) {
            throw new Error("invalid enum entity reference: %s", expr.getQualifiedName());
        }

        if (types.contains(ref)) {
            return ref;
        }
        throw new Error("invalid type reference: %s", expr.getQualifiedName());
    }

    @Override
    public AntlrBuiltinType visitActionExpr(ActionExpression expr) {
        AntlrTypeReference action = AntlrTypeReference.parse(expr.getQualifiedName());
        if (schema.getAction(action) == null) {
            throw new Error("invalid action: " + action);
        }
        AntlrTypeReference ref = AntlrTypeReference.parse(expr.getType());
        if (types.contains(ref)) {
            return ref;
        }
        throw new Error("invalid type reference: %s", ref);
    }

    @Override
    public AntlrBuiltinType visitTypeExpr(TypeExpression expr) {
        AntlrTypeReference ref = AntlrTypeReference.parse(expr.getValue());
        if (types.contains(ref)) {
            return TypeOfEntityType;
        }
        throw new Error("invalid type: %s in type expression: %s. Available types: %s",
                expr.getValue(), expr, types);
    }

    @Override
    public AntlrBuiltinType visitCallExpr(CallExpression expr) {
        String name = expr.getFunc();
        InvariantFunctionValidator.FunctionValidator validator =
                InvariantFunctionValidator.getValidator(name);
        if (validator == null) {
            throw new Error("Function: %s not registered", name);
        }
        AntlrBuiltinType self = expr.getSelf() == null ? null : collect(expr.getSelf());
        return validator.validate(self, collect(expr.getArgs()));
    }

    // == Unsupported
    @Override
    public AntlrBuiltinType visitConditionalExpr(ConditionalExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public AntlrBuiltinType visitSetExpr(SetExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public AntlrBuiltinType visitSlotExpr(SlotExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public AntlrBuiltinType visitPolicy(Policy policy) {
        throw new TranslationError("unsupported element: " + policy);
    }

    @Override
    public AntlrBuiltinType visitInvariant(Invariant invariant) {
        throw new TranslationError("unsupported element: " + invariant);
    }

    @Override
    public AntlrBuiltinType visitQuantifier(Quantifier quantifier) {
        throw new TranslationError("unsupported element: " + quantifier);
    }

    @Override
    public AntlrBuiltinType visitRecordExpr(RecordExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }
}
