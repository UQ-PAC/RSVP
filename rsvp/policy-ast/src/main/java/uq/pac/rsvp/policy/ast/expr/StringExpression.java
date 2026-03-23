package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.StringLiteral;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class StringExpression extends Expression {

    private final String value;
    private final boolean quoted;

    public StringExpression(String value, boolean quoted, SourceLoc source) {
        super(StringLiteral, source);
        this.value = value;
        this.quoted = quoted || !NICE_PROP_NAME.matcher(value).matches();
    }

    public StringExpression(String value, SourceLoc source) {
        this(value, true, source);
    }

    public StringExpression(String value, boolean quoted) {
        this(value, quoted, SourceLoc.MISSING);
    }

    public StringExpression(String value) {
        this(value, SourceLoc.MISSING);
    }

    public StringExpression() {
        this("", SourceLoc.MISSING);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitStringExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitStringExpr(this);
    }

    @Override
    public String toString() {
        return quoted || !NICE_PROP_NAME.matcher(value).matches() ? "\"" + value + "\"" : value;
    }

}
