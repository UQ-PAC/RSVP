package uq.pac.rsvp.policy.datalog.translation;

import org.logicng.formulas.*;
import uq.pac.rsvp.policy.ast.expr.*;

/**
 * Ordering for sub-expressions.
 * For the moment pushes property expressions to the back
*/
public class ExpressionOrdering extends ValueVisitorAdapter<Integer> {

    private static final int BACK = 2;
    private static final int FRONT = 1;

    private ExpressionOrdering() {}

    private static final ExpressionOrdering ORDERING = new ExpressionOrdering();

    public static int order(Expression e) {
        return e.compute(ORDERING);
    }

    @Override
    public Integer visitBinaryExpr(BinaryExpression expr) {
        return Math.max(compute(expr.getLeft()), compute(expr.getRight()));
    }

    @Override
    public Integer visitUnaryExpr(UnaryExpression expr) {
        return compute(expr.getExpression());
    }

    @Override
    public Integer visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return BACK;
    }

    @Override
    public Integer visitRecordExpr(RecordExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitSetExpr(SetExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitVariableExpr(VariableExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitBooleanExpr(BooleanExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitEntityExpr(EntityExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitLongExpr(LongExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitStringExpr(StringExpression expr) {
        return FRONT;
    }

    @Override
    public Integer visitTypeExpr(TypeExpression expr) {
        return FRONT;
    }
}
