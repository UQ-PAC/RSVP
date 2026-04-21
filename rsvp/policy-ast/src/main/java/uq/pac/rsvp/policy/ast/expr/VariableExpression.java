package uq.pac.rsvp.policy.ast.expr;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

import java.util.Objects;

public class VariableExpression extends Expression {

    private final String ref;

    public VariableExpression(String ref, SourceLoc source) {
        super(source);
        this.ref = ref;
    }

    public VariableExpression(String ref) {
        this(ref, SourceLoc.MISSING);
    }

    // Used by Gson
    @SuppressWarnings("unused")
    private VariableExpression() {
        this(null, SourceLoc.MISSING);
    }

    public String getReference() {
        return ref;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitVariableExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitVariableExpr(this);
    }

    @Override
    public String toString() {
        return ref;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other instanceof VariableExpression var) {
            return this.ref.equals(var.ref);
        }
        return false;
    }
}
