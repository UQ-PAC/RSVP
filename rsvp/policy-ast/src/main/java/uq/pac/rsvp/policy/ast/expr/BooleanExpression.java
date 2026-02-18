package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.BooleanLiteral;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class BooleanExpression extends Expression {

    private final boolean value;

    public BooleanExpression(boolean value, SourceLoc source) {
        super(BooleanLiteral, source);
        this.value = value;
    }

    public BooleanExpression(boolean value) {
        this(value, SourceLoc.MISSING);
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitBooleanExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitBooleanExpr(this);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
