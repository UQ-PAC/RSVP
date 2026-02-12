package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.Conditional;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;

public class ConditionalExpression extends Expression {

    private Expression condition;

    private Expression then;

    @SerializedName("else")
    private Expression els;

    public ConditionalExpression(Expression condition, Expression then, Expression els, SourceLoc source) {
        super(Conditional, source);
        this.condition = condition;
        this.then = then;
        this.els = els;
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(if ");
        sb.append(condition.toString());
        sb.append("; then ");
        sb.append(then.toString());
        if (els != null) {
            sb.append("; else ");
            sb.append(els.toString());
        }
        sb.append(')');
        return sb.toString();
    }

}
