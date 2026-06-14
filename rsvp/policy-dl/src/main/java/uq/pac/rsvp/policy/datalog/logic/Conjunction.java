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
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitConjunction(this);
    }
}
