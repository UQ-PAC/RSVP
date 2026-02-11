package uq.pac.rsvp.ast.expr;

import static uq.pac.rsvp.ast.expr.Expression.ExprType.LongLiteral;

import uq.pac.rsvp.ast.SourceLoc;
import uq.pac.rsvp.ast.visitor.PolicyVisitor;;

public class LongExpression extends Expression {

    private long value;

    public LongExpression(long value, SourceLoc source) {
        super(LongLiteral, source);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitLongExpr(this);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
