package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Unary;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class UnaryExpression extends Expression {
    public static enum UnaryOp {
        @SerializedName("not")
        Not,

        @SerializedName("neg")
        Neg
    }

    private final UnaryOp op;
    private final Expression expr;

    public UnaryExpression(UnaryOp op, Expression expr, SourceLoc source) {
        super(Unary, source);
        this.op = op;
        this.expr = expr;
    }

    public UnaryExpression(UnaryOp op, Expression expr) {
        this(op, expr, SourceLoc.MISSING);
    }

    public UnaryOp getOp() {
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
