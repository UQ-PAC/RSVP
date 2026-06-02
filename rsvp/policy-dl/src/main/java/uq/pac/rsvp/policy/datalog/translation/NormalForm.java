package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.policy.expr.*;

import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.BinaryOp.And;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.BinaryOp.Or;
import static uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression.UnaryOp.Not;

public class

public class NNFTransformer extends ExpressionAdapter {
    public Expression visitUnaryExpr(UnaryExpression expr) {
        if (expr.getOp() == Not && expr.getExpression() instanceof BinaryExpression bin &&
                (bin.getOp() == Or || bin.getOp() == And)) {
            BinaryExpression.BinaryOp op = bin.getOp() == And ? Or : And;
                Expression lhs = bin.getLeft().compute(this),
                        rhs = bin.getRight().compute(this);
                return new BinaryExpression(new UnaryExpression(Not, lhs), op, new UnaryExpression(Not, rhs));
        } else {
            return new UnaryExpression(expr.getOp(),
                    expr.getExpression().compute(this),
                    expr.getSourceLoc());
        }
    }
}
