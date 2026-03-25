package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.expr.Expression;

public class Invariant {

    private final Quantifier quantifier;
    private final Expression expression;

    public Invariant(Quantifier quantifier, Expression expression) {
        this.quantifier = quantifier;
        this.expression = expression;
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression + (quantifier == null ? "" : " " + quantifier);
    }
}
