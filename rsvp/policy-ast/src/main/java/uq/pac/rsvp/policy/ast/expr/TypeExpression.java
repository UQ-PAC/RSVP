package uq.pac.rsvp.policy.ast.expr;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Type;

public class TypeExpression extends Expression {
    private final String value;

    public TypeExpression(String value, SourceLoc source) {
        super(Type, source);
        this.value = value;
    }

    public TypeExpression(String value) {
        this(value, SourceLoc.MISSING);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitTypeExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitTypeExpr(this);
    }

    @Override
    public String toString() {
        return value;
    }
}
