package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.expr.Expression;


public class Invariant {
    private final InvariantQuantifier quantifier;
    private final Expression expression;

    public Invariant(InvariantQuantifier quantifier, Expression expression) {
        this.quantifier = quantifier == null ? new InvariantQuantifier() : quantifier;
        this.expression = expression;
    }

    public InvariantQuantifier getQuantifier() {
        return quantifier;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "invariant " + expression + (quantifier.isEmpty() ? "" : "\n    " + quantifier) + ";";
    }
}
