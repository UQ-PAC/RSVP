package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

/**
 * is EntityType
 */
public class IsExpression extends Expression {

    private final Expression expression;
    private final TypeExpression type;

    public IsExpression(Expression expression, TypeExpression type, SourceLoc source) {
        super(source);
        this.expression = expression;
        this.type = type;
    }

    public Expression getExpression() {
        return expression;
    }

    public TypeExpression getTypeExpression() {
        return type;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitIsExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitIsExpr(this);
    }

    @Override
    public String toString() {
        return "(" + expression.toString() +  " is " + type + ")";
    }
}
