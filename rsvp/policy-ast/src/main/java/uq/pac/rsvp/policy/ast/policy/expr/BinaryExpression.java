package uq.pac.rsvp.policy.ast.policy.expr;


import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class BinaryExpression extends Expression {

    public enum BinaryOp {
        Eq,
        Neq,
        And,
        Or,
        Less,
        LessEq,
        Greater,
        GreaterEq,
        Add,
        Sub,
        Mul,
        In,
        Like
    }

    private final BinaryOp op;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(Expression left, BinaryOp op, Expression right, SourceLoc source) {
        super(source);
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
        String opStr = switch (op) {
            case Add -> "+";
            case Sub -> "-";
            case Mul -> "*";
            case Eq -> "==";
            case Neq -> "!=";
            case Greater -> ">";
            case GreaterEq -> ">=";
            case In -> "in";
            case Less -> "<";
            case LessEq -> "<=";
            case Like -> "like";
            case And -> "&&";
            case Or -> "||";

        };
        return '(' + left.toString() + ' ' + opStr + ' ' + right.toString() + ')';
    }
}
