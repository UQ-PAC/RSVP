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

        public static Expression transform(Expression expr) {
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

    static class DNFTransformer extends ExpressionAdapter {

        private static final DNFTransformer TRANSFORMER = new DNFTransformer();

        private DNFTransformer() {}

        public static Expression transform(Expression expr) {
            Expression nnf = NNFTransformer.transform(expr);
            return nnf.compute(TRANSFORMER);
        }

        public Expression visitBinaryExpr(BinaryExpression expr) {
            Expression lhs = expr.getLeft().compute(this);
            Expression rhs = expr.getRight().compute(this);

            BinaryExpression left =
                    lhs instanceof BinaryExpression bin && bin.getOp() == Or ? bin : null;
            BinaryExpression right =
                    rhs instanceof BinaryExpression bin && bin.getOp() == Or ? bin : null;

            Expression result;
            if (left != null && right != null) {
                Expression a = new BinaryExpression(left.getLeft(), And, right.getLeft()),
                    b = new BinaryExpression(left.getLeft(), And, right.getRight()),
                    c = new BinaryExpression(left.getRight(), And, right.getLeft()),
                    d = new BinaryExpression(left.getRight(), And, right.getRight());
                a = new BinaryExpression(a, Or, b);
                c = new BinaryExpression(c, Or, d);
                result = new BinaryExpression(a, Or, c);
            } else if (left != null) {
                Expression a = new BinaryExpression(left.getLeft(), And, rhs),
                        b = new BinaryExpression(left.getRight(), And, rhs);
                result = new BinaryExpression(a, Or, b);
            } else if (right != null) {
                Expression a = new BinaryExpression(lhs, And, right.getLeft()),
                        b = new BinaryExpression(lhs, And, right.getRight());
                result = new BinaryExpression(a, Or, b);
            } else {
                result = new BinaryExpression(lhs, expr.getOp(), rhs, expr.getSourceLoc());
            }

            return result;
        }
    }
}
