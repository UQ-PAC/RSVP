package uq.pac.rsvp.policy.ast.invariant;

import uq.pac.rsvp.policy.ast.PolicyAstNode;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;


public class Invariant extends PolicyAstNode {
    private final Quantifier quantifier;
    private final Expression expression;


    public Invariant(Quantifier quantifier, Expression expression, SourceLoc location) {
        super(location);
        this.quantifier = quantifier == null ? new Quantifier() : quantifier;
        this.expression = expression;
    }

    public Invariant(Quantifier quantifier, Expression expression) {
        this(quantifier, expression, SourceLoc.MISSING);
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "invariant " + expression + (quantifier.isEmpty() ? "" : "\n    " + quantifier) + ";";
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitInvariant(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitInvariant(this);
    }
}
