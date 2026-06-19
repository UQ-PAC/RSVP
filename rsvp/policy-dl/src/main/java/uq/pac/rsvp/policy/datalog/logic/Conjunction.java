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
    public <T> T accept(FormulaValueVisitor<T> visitor) {
        return visitor.visitConjunction(this);
    }

    @Override
    public void accept(FormulaVoidVisitor listener) {
        listener.visitConjunction(this);
    }
}
