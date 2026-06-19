package uq.pac.rsvp.policy.datalog.logic;

public class Term<E> extends Formula {
    private final E value;

    public Term(E value) {
        this.value = value;
    }

    protected String stringify() {
        return value.toString();
    }

    @Override
    public <T> T accept(FormulaValueVisitor<T> visitor) {
        return visitor.visitPredicate(this);
    }

    @Override
    public void accept(FormulaVoidVisitor listener) {
        listener.visitPredicate(this);
    }

    public E getValue() {
        return value;
    }
}
