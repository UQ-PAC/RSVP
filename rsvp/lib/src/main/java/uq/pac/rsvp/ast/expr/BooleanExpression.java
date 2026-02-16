package uq.pac.rsvp.ast.expr;

import static uq.pac.rsvp.ast.expr.Expression.ExprType.BooleanLiteral;

import uq.pac.rsvp.ast.SourceLoc;
import uq.pac.rsvp.ast.visitor.PolicyVisitor;;

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
