package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.policy.expr.Expression;
import uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.policy.Quantifier;
import uq.pac.rsvp.policy.ast.policy.Invariant;

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
        if (invariant.getQuantifier().getScope() == Quantifier.Scope.ALL) {
            Quantifier q = new Quantifier(Quantifier.Scope.NONE, invariant.getQuantifier().getVariables());
            Expression e = new UnaryExpression(UnaryExpression.Operator.Not, invariant.getExpression());
            invariant = new Invariant(q, e);
        }
        return invariant;
    }
}
