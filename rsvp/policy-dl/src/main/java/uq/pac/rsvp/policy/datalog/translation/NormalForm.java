package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.policy.expr.*;

import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.BinaryOp.And;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.BinaryOp.Or;
import static uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression.UnaryOp.Not;

/**
 * Converting expressions to a normal form, such as NNF or CND
 */
public class NormalForm {

    static class NNFTransformer extends ExpressionAdapter {
        private static final NNFTransformer TRANSFORMER = new NNFTransformer();

        private NNFTransformer() { }

        Expression transform(Expression expr) {
            return expr.compute(TRANSFORMER);
        }

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

    static class CNFTransformer extends ExpressionAdapter {

    }
}
