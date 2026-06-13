package uq.pac.rsvp.policy.datalog.logic;

public abstract class LogicalOperator extends Formula {
    private final Formula left;
    private final Formula right;

    public LogicalOperator(Formula left, Formula right) {
        this.left = left;
        this.right = right;
    }

    abstract String getStringOperator();

    protected String stringify() {
        return "(%s %s %s)".formatted(left.toString(), getStringOperator(), right);
    }

    public Formula getLeft() {
        return left;
    }

    public Formula getRight() {
        return right;
    }
}
