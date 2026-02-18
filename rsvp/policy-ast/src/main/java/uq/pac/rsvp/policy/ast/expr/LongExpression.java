package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.LongLiteral;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class LongExpression extends Expression {

    private final long value;

    public LongExpression(long value, SourceLoc source) {
        super(LongLiteral, source);
        this.value = value;
    }

    public LongExpression(long value) {
        this(value, SourceLoc.MISSING);
    }

    public long getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitLongExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitLongExpr(this);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
