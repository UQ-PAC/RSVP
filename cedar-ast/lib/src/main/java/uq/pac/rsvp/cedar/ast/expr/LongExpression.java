package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.LongLiteral;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;;

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
