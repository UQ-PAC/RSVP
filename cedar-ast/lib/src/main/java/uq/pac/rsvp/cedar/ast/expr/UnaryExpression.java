package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.Unary;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;

public class UnaryExpression extends Expression {
    public static enum UnaryOp {
        @SerializedName("not")
        Not,

        @SerializedName("neg")
        Neg
    }

    private UnaryOp op;
    private Expression expr;

    public UnaryExpression(UnaryOp op, Expression expr, SourceLoc source) {
        super(Unary, source);
        this.op = op;
        this.expr = expr;
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
