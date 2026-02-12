package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.BooleanLiteral;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;;

public class BooleanExpression extends Expression {

    private boolean value;

    public BooleanExpression(boolean value, SourceLoc source) {
        super(BooleanLiteral, source);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitBooleanExpr(this);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
