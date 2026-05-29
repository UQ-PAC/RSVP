package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

/**
 * has attr | has "attr"
 */
public class HasExpression extends Expression {

    private final Expression expression;
    private final String attribute;

    public HasExpression(Expression expression, String attribute, SourceLoc source) {
        super(source);
        this.expression = expression;
        this.attribute = attribute;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitHasExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitHasExpr(this);
    }

    @Override
    public String toString() {
        return "(" + expression.toString() +  " has \"" + attribute + "\")";
    }
}
