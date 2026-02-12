package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.StringLiteral;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;;

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
