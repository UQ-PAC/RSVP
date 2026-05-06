package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class ConditionalExpression extends Expression {

    private final Expression condition;

    private final Expression then;

    private final Expression els;

    public ConditionalExpression(Expression condition, Expression then, Expression els, SourceLoc source) {
        super(source);
        this.condition = condition;
        this.then = then;
        this.els = els;
    }

    public ConditionalExpression(Expression condition, Expression then, Expression els) {
        this(condition, then, els, SourceLoc.MISSING);
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getThen() {
        return then;
    }

    public Expression getElse() {
        return els;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitConditionalExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitConditionalExpr(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(if ");
        sb.append(condition.toString());
        sb.append("; then ");
        sb.append(then.toString());
        if (els != null) {
            sb.append("; else ");
            sb.append(els);
        }
        sb.append(')');
        return sb.toString();
    }

}
