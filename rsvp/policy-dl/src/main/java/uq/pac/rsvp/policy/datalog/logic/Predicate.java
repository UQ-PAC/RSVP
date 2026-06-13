package uq.pac.rsvp.policy.datalog.logic;

public class Predicate<E> extends Formula {
    private final E value;

    private Predicate(E value) {
        this.value = value;
    }

    protected String stringify() {
        return value.toString();
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitPredicate(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } if (other == this) {
            return true;
        } else if (other instanceof Predicate<?> predicate) {
            return this.value.equals(predicate.value);
        }
        return false;
    }
}
