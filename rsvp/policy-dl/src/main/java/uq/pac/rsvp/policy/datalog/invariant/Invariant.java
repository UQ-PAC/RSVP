package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.expr.Expression;


public class Invariant {
    private final String name;
    private final Quantifier quantifier;
    private final Expression expression;

    public Invariant(String name, Quantifier quantifier, Expression expression) {
        this.name = name;
        this.quantifier = quantifier;
        this.expression = expression;
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "@invariant(\"" + name + "\")\n" + expression + (quantifier == null ? "" : "\n    " + quantifier);
    }
}
