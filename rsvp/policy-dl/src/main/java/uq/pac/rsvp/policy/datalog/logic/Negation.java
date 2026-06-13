package uq.pac.rsvp.policy.datalog.logic;

public class Negation extends Formula {
    private final Formula formula;

    public Negation(Formula formula) {
        this.formula = formula;
    }

    protected String stringify() {
        return "!" + formula.toString();
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitNegation(this);
    }

    public Formula getFormula() {
        return formula;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } if (other == this) {
            return true;
        } else if (other instanceof Negation n) {
            return this.formula.equals(n.formula);
        }
        return false;
    }
}
