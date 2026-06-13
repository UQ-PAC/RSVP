package uq.pac.rsvp.policy.datalog.logic;

public class Conjunction extends LogicalOperator {
    public Conjunction(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    String getStringOperator() {
        return "&&";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof Conjunction c) {
            return getLeft().equals(c.getLeft()) &&
                    getRight().equals(c.getRight());
        }
        return false;
    }
}
