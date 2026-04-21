package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationConstants;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.*;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.invariant.InvariantTyping.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class InvariantValidator implements PolicyComputationVisitor<CommonTypeDefinition> {
    private final Map<String, RecordTypeDefinition> types;
    private final Map<String, RecordTypeDefinition> variables;
    private final Map<String, RecordTypeDefinition> entities;
    private final Map<String, RecordTypeDefinition> actions;

    private InvariantValidator(InvariantValidator factory, uq.pac.rsvp.policy.ast.invariant.Invariant invariant) {
        this.types = Map.copyOf(factory.types);
        this.entities = Map.copyOf(factory.entities);
        this.actions = Map.copyOf(factory.actions);
        this.variables = getVariables(invariant, types);
    }

    /**
     * Public constructor. Acts like a factory in that it builds internal structures,
     * such as available types, entities and actions. It deliberately leaves invariant-specific
     * structures (i.e., variables) nullified. Invariant validation is done via the private
     * constructor that copies general data from this factory object, computes invariant-specific
     * information and does validation.
     */
    public InvariantValidator(Schema schema, EntitySet entities) {
        this.types = new HashMap<>();
        this.variables = null;

        InvariantTyping typing = new InvariantTyping();

        // Build types from entities
        schema.entityTypes().stream()
                .map(typing::convert)
                .forEach(e -> types.put(e.getName(), e));
        // Build types from actions
        schema.actions().stream()
                .map(typing::convert)
                .forEach(e -> types.put(e.getName(), e));

        // FIXME: ensure entities and actions have types
        this.entities = entities.getEntities().stream().collect(Collectors.toMap(
                e -> e.getEuid().getReference(),
                e -> types.get(e.getEuid().getType())));

		// Put in undefined references
        schema.entityTypes().stream()
                .filter(e -> e.getEntityNamesEnum().isEmpty())
                .map(TranslationConstants::getUndefinedEUID)
                .forEach(uid -> this.entities.put(uid.toCedarExpr(), types.get(uid.getType().toString())));

        this.actions = schema.actions().stream().collect(Collectors.toMap(
            ActionDefinition::getQualifiedName, a -> types.get(a.getType())
        ));
    }

    private static Map<String, RecordTypeDefinition> getVariables(uq.pac.rsvp.policy.ast.invariant.Invariant invariant, Map<String, RecordTypeDefinition> types) {
        Map<String, RecordTypeDefinition> variables = new HashMap<>();
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

    private CommonTypeDefinition collect(Expression expr) {
        return Objects.requireNonNull(expr.compute(this));
    }

    private List<CommonTypeDefinition> collect(Collection<Expression> exprs) {
        return exprs.stream().map(this::collect).toList();
    }

    public void validate(uq.pac.rsvp.policy.ast.invariant.Invariant invariant) {
        require(this.variables == null);
        InvariantValidator validator = new InvariantValidator(this, invariant);
        expect(validator.collect(invariant.getExpression()), TBoolean);
    }

    @Override
    public CommonTypeDefinition visitBinaryExpr(BinaryExpression expr) {
        CommonTypeDefinition lhs = collect(expr.getLeft());
        CommonTypeDefinition rhs = collect(expr.getRight());

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
    public CommonTypeDefinition visitPropertyAccessExpr(PropertyAccessExpression expr) {
        CommonTypeDefinition objectType = collect(expr.getObject());
        if (objectType instanceof EntityTypeReference ref) {
            objectType = types.get(ref.getDefinition().getName());
        }
        if (objectType instanceof RecordTypeDefinition rec) {
            CommonTypeDefinition attrType = rec.getAttributeType(expr.getProperty());
            if (attrType != null) {
                return attrType;
            }
        }
        throw new Error("Invalid property access: %s [%s: %s]", expr, expr.getObject(), InvariantTyping.name(objectType));
    }

    @Override
    public CommonTypeDefinition visitUnaryExpr(UnaryExpression expr) {
        CommonTypeDefinition type = collect(expr.getExpression());
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
    public CommonTypeDefinition visitBooleanExpr(BooleanExpression expr) {
        return BooleanType;
    }

    @Override
    public CommonTypeDefinition visitVariableExpr(VariableExpression expr) {
        String ref = expr.getReference();
        if (variables.containsKey(ref)) {
            return variables.get(ref);
        }
        throw new Error("Ungrounded variable: %s", ref);
    }

    @Override
    public CommonTypeDefinition visitLongExpr(LongExpression expr) {
        return LongType;
    }

    @Override
    public CommonTypeDefinition visitStringExpr(StringExpression expr) {
        return StringType;
    }

    @Override
    public CommonTypeDefinition visitEntityExpr(EntityExpression expr) {
        String name = expr.getQualifiedName();
        if (entities.containsKey(name)) {
            return entities.get(name);
        }
        throw new Error("invalid type reference: %s", name);
    }

    @Override
    public CommonTypeDefinition visitActionExpr(ActionExpression expr) {
        String name = expr.getQualifiedName();
        if (actions.containsKey(name)) {
            return actions.get(name);
        }
        throw new Error("invalid type reference: %s", name);
    }

    @Override
    public CommonTypeDefinition visitTypeExpr(TypeExpression expr) {
        if (types.containsKey(expr.getValue())) {
            return TypeOfEntityType;
        }
        throw new Error("invalid type: %s in type expression: %s. Available types: %s",
                expr.getValue(), expr, types.keySet());
    }

    @Override
    public CommonTypeDefinition visitCallExpr(CallExpression expr) {
        String name = expr.getFunc();
        InvariantFunctionValidator.FunctionValidator validator = InvariantFunctionValidator.getValidator(name);
        if (validator == null) {
            throw new Error("Function: %s not registered", name);
        }
        CommonTypeDefinition self = expr.getSelf() == null ? null : collect(expr.getSelf());
        return validator.validate(self, collect(expr.getArgs()));
    }

    // == Unsupported
    @Override
    public CommonTypeDefinition visitConditionalExpr(ConditionalExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public CommonTypeDefinition visitSetExpr(SetExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public CommonTypeDefinition visitSlotExpr(SlotExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

    @Override
    public CommonTypeDefinition visitPolicySet(PolicySet policies) {
        throw new TranslationError("unsupported element: " + policies);
    }

    @Override
    public CommonTypeDefinition visitPolicy(Policy policy) {
        throw new TranslationError("unsupported element: " + policy);
    }

    @Override
    public CommonTypeDefinition visitRecordExpr(RecordExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }
}
