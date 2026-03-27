package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.*;
import java.util.stream.Collectors;

public class InvariantValidation implements PolicyComputationVisitor<CommonTypeDefinition> {

    private final Map<String, RecordTypeDefinition> types;
    private final Map<String, RecordTypeDefinition> variables;
    private final Invariant invariant;

    public static class Error extends RuntimeException {
        public Error(Object format, Object ...args) {
            super("Invariant validation error: " + String.format(format.toString(), args));
        }
    }

    public InvariantValidation(Schema schema, Invariant invariant) {
        this.types = new HashMap<>();
        this.variables = new HashMap<>();
        this.invariant = invariant;

        schema.entityTypes().forEach(e -> {
            Map<String, CommonTypeDefinition> attributes = e.getShapeAttributeNames().stream()
                    .collect(Collectors.toMap(attr -> attr, e::getShapeAttributeType));
            types.put(e.getName(), new RecordTypeDefinition(e.getName(), attributes));
        });

        invariant.getQuantifier().getVariables().forEach((var, type) -> {
            if (!types.containsKey(type)) {
                throw new Error("invalid type: %s in quantifier: %s\nAvailable types: %s",
                        type, invariant.getQuantifier(), types.keySet());
            }
            variables.put(var, types.get(type));
        });
    }

    private final static BooleanType TBoolean = new BooleanType();
    private final static StringType TString = new StringType();
    private final static LongType TLong = new LongType();

    private String typename(CommonTypeDefinition type) {
        return switch (type) {
            case BooleanType b -> "Boolean";
            case LongType l -> "Long";
            case StringType s -> "String";
            case RecordTypeDefinition r -> r.getName();
            default -> throw new AssertionError("Unsupported type: " + type);
        };
    }

    private CommonTypeDefinition unsupported(Object o) {
        throw new AssertionError("unsupported element: " + o);
    }

    private void expect(Expression expr, CommonTypeDefinition expectedType, CommonTypeDefinition ...actualTypes) {
        for (CommonTypeDefinition actualType : actualTypes) {
            if (!expectedType.equals(actualType)) {
                throw new Error("Expected %s type, got %s in expression: %s",
                        typename(expectedType), typename(actualType), expr);
            }
        }
    }

    public void validate(Invariant invariant) {
        expect(invariant.getExpression(), TBoolean, invariant.getExpression().compute(this));
    }

    @Override
    public CommonTypeDefinition visitBinaryExpr(BinaryExpression expr) {
        CommonTypeDefinition lhs = expr.getLeft().compute(this);
        CommonTypeDefinition rhs = expr.getRight().compute(this);

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
                yield TBoolean;
            }
            case Or, And -> {
                expect(expr, TBoolean, lhs, rhs);
                yield TBoolean;
            }
            default -> throw new TranslationError("Unsupported");
        };
    }

    @Override
    public CommonTypeDefinition visitPropertyAccessExpr(PropertyAccessExpression expr) {
        CommonTypeDefinition objectType = expr.getObject().compute(this);
        if (objectType instanceof RecordTypeDefinition rec) {
            CommonTypeDefinition attrType = rec.getAttributeType(expr.getProperty());
            if (attrType != null) {
                return attrType;
            }
        }
        throw new Error("Invalid property access: %s [%s: %s]", expr, expr.getObject(), typename(objectType));
    }

    @Override
    public CommonTypeDefinition visitUnaryExpr(UnaryExpression expr) {
        CommonTypeDefinition type = expr.getExpression().compute(this);
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
    public CommonTypeDefinition visitCallExpr(CallExpression expr) {
        return unsupported(expr);
    }

    @Override
    public CommonTypeDefinition visitConditionalExpr(ConditionalExpression expr) {
        return unsupported(expr);
    }

    @Override
    public CommonTypeDefinition visitSetExpr(SetExpression expr) {
        return unsupported(expr);
    }

    @Override
    public CommonTypeDefinition visitEntityExpr(EntityExpression expr) {
        return unsupported(expr);
    }

    @Override
    public CommonTypeDefinition visitActionExpr(ActionExpression expr) {
        return unsupported(expr);
    }

    @Override
    public CommonTypeDefinition visitTypeExpr(TypeExpression expr) {
        return unsupported(expr);
    }

    // == Unsupported
    @Override
    public CommonTypeDefinition visitSlotExpr(SlotExpression expr) {
        return unsupported(expr);
    }

    @Override
    public CommonTypeDefinition visitPolicySet(PolicySet policies) {
        return unsupported(policies);
    }

    @Override
    public CommonTypeDefinition visitPolicy(Policy policy) {
        return unsupported(policy);
    }

    @Override
    public CommonTypeDefinition visitRecordExpr(RecordExpression expr) {
        return unsupported(expr);
    }
}
