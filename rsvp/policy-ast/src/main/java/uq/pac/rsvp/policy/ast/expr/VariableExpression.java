package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Variable;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class VariableExpression extends Expression {

    private final String ref;

    public VariableExpression(String ref, SourceLoc source) {
        super(Variable, source);
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
}
