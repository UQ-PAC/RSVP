package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class UnaryExpression extends Expression {
    public enum Operator {
        Not,
        Neg
    }

    private final Operator op;
    private final Expression expr;

    public UnaryExpression(Operator op, Expression expr, SourceLoc source) {
        super(source);
        this.op = op;
        this.expr = expr;
    }

    public UnaryExpression(Operator op, Expression expr) {
        this(op, expr, SourceLoc.MISSING);
    }

    public Operator getOp() {
        return op;
    }

    public Expression getExpression() {
        return expr;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitUnaryExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }

    @Override
    public <T, P> T compute(PolicyPayloadVisitor<T, P> visitor, P payload) {
        return visitor.visitUnaryExpr(this, payload);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (op) {
            case Neg:
                sb.append('-');
                break;
            case Not:
                sb.append('!');
        }
        sb.append(expr.toString());
        return sb.toString();
    }
}
