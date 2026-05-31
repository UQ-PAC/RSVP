package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class StringExpression extends Expression {

    private final String value;

    public StringExpression(String value, SourceLoc source) {
        super(source);
        this.value = value;
    }

    public StringExpression(String value) {
        this(value, SourceLoc.MISSING);
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
        return "\"" + value + "\"";
    }
}
