package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Binary;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class BinaryExpression extends Expression {

    public static enum BinaryOp {
        @SerializedName("eq")
        Eq,

        @SerializedName("neq")
        Neq,

        @SerializedName("and")
        And,

        @SerializedName("or")
        Or,

        @SerializedName("lt")
        Less,

        @SerializedName("leq")
        LessEq,

        @SerializedName("gt")
        Greater,

        @SerializedName("geq")
        GreaterEq,

        @SerializedName("add")
        Add,

        @SerializedName("sub")
        Sub,

        @SerializedName("mul")
        Mul,

        @SerializedName("in")
        In,

        @SerializedName("like")
        Like,

        @SerializedName("is")
        Is,

        @SerializedName("has")
        HasAttr
    }

    private BinaryOp op;
    private Expression left;
    private Expression right;

    public BinaryExpression(Expression left, BinaryOp op, Expression right, SourceLoc source) {
        super(Binary, source);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public BinaryOp getOp() {
        return op;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitBinaryExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitBinaryExpr(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(left.toString());
        sb.append(' ');
        switch (op) {
            case Add:
                sb.append("+");
                break;
            case And:
                sb.append("&&");
                break;
            case Eq:
                sb.append("==");
                break;
            case Greater:
                sb.append(">");
                break;
            case GreaterEq:
                sb.append(">=");
                break;
            case HasAttr:
                sb.append("has");
                break;
            case In:
                sb.append("in");
                break;
            case Is:
                sb.append("is");
                break;
            case Less:
                sb.append("<");
                break;
            case LessEq:
                sb.append("<=");
                break;
            case Like:
                sb.append("like");
                break;
            case Mul:
                sb.append("*");
                break;
            case Neq:
                sb.append("!=");
                break;
            case Or:
                sb.append("||");
                break;
            case Sub:
                sb.append("-");
                break;
        }
        sb.append(' ');
        sb.append(right.toString());
        sb.append(')');

        return sb.toString();
    }
}
