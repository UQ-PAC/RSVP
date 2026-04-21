package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;

/**
 * Transformation for invariants.
 * For the moment the only supported transformation is ALL -> NONE,
 * i.e.,
 * <pre>
 * expr for all v: Type
 *    becomes
 * expr for none v : Type
 * </pre>
 */
public class InvariantTransformer {

    public static Invariant transform(Invariant invariant) {
        if (invariant.getQuantifier().getScope() == InvariantQuantifier.Scope.ALL) {
            InvariantQuantifier q = new InvariantQuantifier(InvariantQuantifier.Scope.NONE, invariant.getQuantifier().getVariables());
            Expression e = new UnaryExpression(UnaryExpression.UnaryOp.Not, invariant.getExpression());
            invariant = new Invariant(q, e);
        }
        return invariant;
    }
}
