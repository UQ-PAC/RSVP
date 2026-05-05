package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrAction;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.invariant.Quantifier;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationConstants;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.*;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.invariant.InvariantTyping.*;
import static uq.pac.rsvp.Assertion.require;

public class InvariantValidator implements PolicyComputationVisitor<AntlrBuiltinType> {
    private final Map<String, AntlrRecordType> types;
    private final Map<String, AntlrRecordType> variables;
    private final Map<String, AntlrRecordType> entities;
    private final Map<String, AntlrRecordType> actions;
    private final InvariantTyping typing;

    private InvariantValidator(InvariantValidator factory, Invariant invariant) {
        this.types = Map.copyOf(factory.types);
        this.entities = Map.copyOf(factory.entities);
        this.actions = Map.copyOf(factory.actions);
        this.variables = getVariables(invariant, types);
        this.typing = factory.typing;
    }

    /**
     * Public constructor. Acts like a factory in that it builds internal structures,
     * such as available types, entities and actions. It deliberately leaves invariant-specific
     * structures (i.e., variables) nullified. Invariant validation is done via the private
     * constructor that copies general data from this factory object, computes invariant-specific
     * information and does validation.
     */
    public InvariantValidator(AntlrSchema schema, EntitySet entities) {
        this.types = new HashMap<>();
        this.variables = null;
        this.typing = new InvariantTyping(schema);

        // Build types from entities
        schema.entityTypes()
                .forEach(e -> types.put(e.getName(), typing.convert(e)));
        // Build types from actions
        schema.actions()
                .forEach(e -> types.put(e.getName(), new AntlrRecordType()));

        // FIXME: ensure entities and actions have types
        this.entities = entities.getEntities().stream().collect(Collectors.toMap(
                e -> e.getEuid().getReference(),
                e -> types.get(e.getEuid().getType())));

		// Put in undefined references
        schema.enumEntityTypes()
                .filter(e -> e.getEnumNames().isEmpty())
                .map(TranslationConstants::getUndefinedEUID)
                .forEach(uid -> this.entities.put(uid.toCedarExpr(), types.get(uid.getType().toString())));

        this.actions = schema.actions().collect(Collectors.toMap(
            AntlrAction::getName, a -> types.get(a.getNamespace())
        ));
    }

    private static Map<String, AntlrRecordType> getVariables(Invariant invariant, Map<String, AntlrRecordType> types) {
        Map<String, AntlrRecordType> variables = new HashMap<>();
        invariant.getQuantifier().getVariables().forEach(var -> {
            if (!types.containsKey(var.type().getValue())) {
                throw new Error("invalid type: %s in quantifier: %s. Available types: %s",
                        var.type(), invariant.getQuantifier(), types.keySet());
            }
            if (variables.containsKey(var.name().getReference())) {
                throw new Error("duplicate variable name: %s in quantifier: %s",
                        var.name(), invariant.getQuantifier());
            }
            variables.put(var.name().getReference(), types.get(var.type().getValue()));
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
                expectCompatible(lhs, rhs, TBoolean, TLong, TString, typing.TEntityOrAction);
                yield BooleanType;
            }
            case Or, And -> {
                expectCompatible(lhs, rhs, TBoolean);
                yield BooleanType;
            }
            case HasAttr -> {
                expect(lhs, typing.TEntityOrAction, TRecord);
                expect(rhs, TString);
                yield BooleanType;
            }
            case Is -> {
                expect(lhs, typing.TEntityOrAction);
                expect(rhs, TTypeOfEntity);
                yield BooleanType;
            }
            case In -> {
                expectCompatible(lhs, rhs, typing.TEntityOrAction);
                yield BooleanType;
            }
            default -> throw new TranslationError("Unsupported");
        };
    }

    @Override
    public AntlrBuiltinType visitPropertyAccessExpr(PropertyAccessExpression expr) {
        AntlrBuiltinType objectType = collect(expr.getObject());
        // FIXME: Commented out
//        if (objectType instanceof AntlrTypeReference ref) {
//            objectType = types.get(ref.getDefinition().getName());
//        }
//        if (objectType instanceof AntlrRecordType rec) {
//            AntlrBuiltinType attrType = rec.getAttributeType(expr.getProperty());
//            if (attrType != null) {
//                return attrType;
//            }
//        }
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
        String name = expr.getQualifiedName();
        if (entities.containsKey(name)) {
            return entities.get(name);
        }
        throw new Error("invalid type reference: %s", name);
    }

    @Override
    public AntlrBuiltinType visitActionExpr(ActionExpression expr) {
        String name = expr.getQualifiedName();
        if (actions.containsKey(name)) {
            return actions.get(name);
        }
        throw new Error("invalid type reference: %s", name);
    }

    @Override
    public AntlrBuiltinType visitTypeExpr(TypeExpression expr) {
        if (types.containsKey(expr.getValue())) {
            return TypeOfEntityType;
        }
        throw new Error("invalid type: %s in type expression: %s. Available types: %s",
                expr.getValue(), expr, types.keySet());
    }

    @Override
    public AntlrBuiltinType visitCallExpr(CallExpression expr) {
        String name = expr.getFunc();
        InvariantFunctionValidator.FunctionValidator validator =
                InvariantFunctionValidator.getValidator(name, typing);
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
