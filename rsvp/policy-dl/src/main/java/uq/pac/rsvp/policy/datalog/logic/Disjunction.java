package uq.pac.rsvp.policy.datalog.logic;

public class Disjunction extends LogicalOperator {
    public Disjunction(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    String getStringOperator() {
        return "||";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof Disjunction d) {
            return getLeft().equals(d.getLeft()) &&
                    getRight().equals(d.getRight());
        }
        return false;
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitDisjunction(this);
    }
}
