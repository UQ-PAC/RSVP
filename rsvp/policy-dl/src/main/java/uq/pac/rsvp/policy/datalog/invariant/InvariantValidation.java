package uq.pac.rsvp.policy.datalog.invariant;

import com.cedarpolicy.model.entity.Entities;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.*;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.invariant.Typing.*;

public class InvariantValidation implements PolicyComputationVisitor<CommonTypeDefinition> {

    private final Typing typing = new Typing();
    private final Map<String, RecordTypeDefinition> types;
    private final Map<String, RecordTypeDefinition> variables;
    private final Map<String, RecordTypeDefinition> entities;
    private final Map<String, RecordTypeDefinition> actions;
    private final Invariant invariant;

    public static class Error extends RuntimeException {
        public Error(Object format, Object ...args) {
            super("Invariant validation error: " + String.format(format.toString(), args));
        }
    }

    private CommonTypeDefinition collect(Expression expr) {
        return Objects.requireNonNull(expr.compute(this));
    }

    public InvariantValidation(Schema schema, Entities entities, Invariant invariant) {
        this.types = new HashMap<>();
        this.variables = new HashMap<>();
        this.invariant = invariant;

        // Build types from entities
        schema.entityTypes().stream()
                .map(typing::convert)
                .forEach(e -> types.put(e.getName(), e));
        // Build types from actions
        schema.actions().stream()
                .map(typing::convert)
                .forEach(e -> types.put(e.getName(), e));
        // Build variables cross-checking types
        invariant.getQuantifier().getVariables().forEach(var -> {
            if (!types.containsKey(var.type())) {
                throw new Error("invalid type: %s in quantifier: %s. Available types: %s",
                        var.type(), invariant.getQuantifier(), types.keySet());
            }
            if (variables.containsKey(var.name())) {
                throw new Error("duplicate variable name: %s in quantifier: %s",
                        var.name(), invariant.getQuantifier());
            }
            variables.put(var.name(), types.get(var.type()));
        });

        // FIXME: ensure entities and actions have types
        this.entities = entities.getEntities().stream().collect(Collectors.toMap(
                e -> e.getEUID().toCedarExpr(),
                e -> types.get(e.getEUID().getType().toString())));

        this.actions = schema.actions().stream().collect(Collectors.toMap(
            ActionDefinition::getQualifiedName, a -> types.get(a.getType())
        ));
    }

    private void expectEntityType(Expression expr, CommonTypeDefinition t) {
        if (t instanceof RecordTypeDefinition rec) {
            String type = rec.getName();
            if (!type.endsWith("::Action") && !type.equals("Action")) {
                return;
            }
        }
        throw new Error("Expected entity type, got: " + Typing.name(t) + " in expression: " + expr);
    }

    private void expectRecord(Expression expr, CommonTypeDefinition t) {
        if (!(t instanceof RecordTypeDefinition)) {
            throw new Error("expected Record, Entity or Action, got " + Typing.name(t) + " in expression: " + expr);
        }
    }

    /**
     * Types supported by the equality operator so far
     */
    private void expectEquatable(Expression expr, CommonTypeDefinition t) {
        boolean eq =  switch (t) {
            case BooleanType b -> true;
            case LongType l -> true;
            case StringType s -> true;
            case EntityTypeReference r -> true;
            case RecordTypeDefinition r -> r.getName() != null;
            default -> false;
        };

        if (!eq) {
            throw new Error("expected Bool, String, Long, Entity or Action, got "
                    + Typing.name(t) + " in expression: " + expr);
        }
    }

    private void expect(Expression expr, CommonTypeDefinition expectedType, CommonTypeDefinition ...actualTypes) {
        for (CommonTypeDefinition actualType : actualTypes) {
            if (!expectedType.equals(actualType)) {
                throw new Error("Expected %s type, got %s in expression: %s",
                        Typing.name(expectedType), Typing.name(actualType), expr);
            }
        }
    }

    public void validate(Invariant invariant) {
        expect(invariant.getExpression(), TBoolean, collect(invariant.getExpression()));
    }

    @Override
    public CommonTypeDefinition visitBinaryExpr(BinaryExpression expr) {
        CommonTypeDefinition lhs = collect(expr.getLeft());
        CommonTypeDefinition rhs = collect(expr.getRight());

        return switch (expr.getOp()) {
            case Add, Sub, Mul -> {
                expect(expr, TLong, lhs, rhs);
                yield TLong;
            }
            case Less, LessEq, Greater, GreaterEq -> {
                expect(expr, TLong, lhs, rhs);
                yield TBoolean;
            }
            case Eq, Neq -> {
                expect(expr, lhs, rhs);
                expectEquatable(expr, lhs);
                yield TBoolean;
            }
            case Or, And -> {
                expect(expr, TBoolean, lhs, rhs);
                yield TBoolean;
            }
            case HasAttr -> {
                expectRecord(expr, lhs);
                expect(expr, TString, rhs);
                yield TBoolean;
            }
            // FIXME: Need to check how in behaves WRT to different types
            case Is -> {
                expectRecord(expr, lhs);
                expect(expr, TEntityType, rhs);
                yield TBoolean;
            }
            // FIXME: Need to check how in behaves WRT to different types
            case In -> {
                expectEntityType(expr, lhs);
                expectEntityType(expr, rhs);
                yield TBoolean;
            }
            default -> throw new TranslationError("Unsupported");
        };
    }

    @Override
    public CommonTypeDefinition visitPropertyAccessExpr(PropertyAccessExpression expr) {
        CommonTypeDefinition objectType = collect(expr.getObject());
        if (objectType instanceof RecordTypeDefinition rec) {
            CommonTypeDefinition attrType = rec.getAttributeType(expr.getProperty());
            if (attrType != null) {
                return attrType;
            }
        }
        throw new Error("Invalid property access: %s [%s: %s]", expr, expr.getObject(), Typing.name(objectType));
    }

    @Override
    public CommonTypeDefinition visitUnaryExpr(UnaryExpression expr) {
        CommonTypeDefinition type = collect(expr.getExpression());
        return switch (expr.getOp()) {
            case Not -> {
                expect(expr, TBoolean, type);
                yield TBoolean;
            }
            case Neg -> {
                expect(expr, TLong, type);
                yield TLong;
            }
        };
    }

    @Override
    public CommonTypeDefinition visitBooleanExpr(BooleanExpression expr) {
        return TBoolean;
    }

    @Override
    public CommonTypeDefinition visitVariableExpr(VariableExpression expr) {
        String ref = expr.getReference();
        if (variables.containsKey(ref)) {
            return variables.get(ref);
        }
        throw new Error("Ungrounded variable: %s in %s", ref, invariant.getExpression());
    }

    @Override
    public CommonTypeDefinition visitLongExpr(LongExpression expr) {
        return TLong;
    }

    @Override
    public CommonTypeDefinition visitStringExpr(StringExpression expr) {
        return TString;
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
            return TEntityType;
        }
        throw new Error("invalid type: %s in type expression: %s. Available types: %s",
                expr.getValue(), expr, types.keySet());
    }

    // == Unsupported

    @Override
    public CommonTypeDefinition visitCallExpr(CallExpression expr) {
        throw new TranslationError("unsupported element: " + expr);
    }

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
