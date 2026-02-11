package uq.pac.rsvp.ast.expr;

import static uq.pac.rsvp.ast.expr.Expression.ExprType.StringLiteral;

import uq.pac.rsvp.ast.SourceLoc;
import uq.pac.rsvp.ast.visitor.PolicyVisitor;;

public class StringExpression extends Expression {

    private String value;

    public StringExpression(String value, SourceLoc source) {
        super(StringLiteral, source);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitStringExpr(this);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

}
