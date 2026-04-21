package uq.pac.rsvp.policy.ast.expr;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class TypeExpression extends Expression {
    private final String value;

    public TypeExpression(String value, SourceLoc source) {
        super(source);
        this.value = value;
    }

    public TypeExpression(String value) {
        this(value, SourceLoc.MISSING);
    }

    // Used by Gson
    @SuppressWarnings("unused")
    private TypeExpression() {
        this(null, SourceLoc.MISSING);
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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof TypeExpression te) {
            return this.value.equals(te.value);
        }
        return false;
    }
}
