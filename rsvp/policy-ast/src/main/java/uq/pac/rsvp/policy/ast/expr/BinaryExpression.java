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

    private final BinaryOp op;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(Expression left, BinaryOp op, Expression right, SourceLoc source) {
        super(Binary, source);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public BinaryExpression(Expression left, BinaryOp op, Expression right) {
        this(left, op, right, SourceLoc.MISSING);
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

        String opStr = switch (op) {
            case Add -> "+";
            case And -> "&&";
            case Eq -> "==";
            case Greater -> "=";
            case GreaterEq -> ">=";
            case HasAttr -> "has";
            case In -> "in";
            case Is -> "is";
            case Less -> "<";
            case LessEq -> "<=";
            case Like -> "like";
            case Mul -> "*";
            case Neq -> "!=";
            case Or -> "||";
            case Sub -> "-";
            default -> "error";
        };

        sb.append(opStr);
        sb.append(' ');
        sb.append(right.toString());
        sb.append(')');

        return sb.toString();
    }
}
