package uq.pac.rsvp.policy.datalog.logic;

public class Predicate<T> extends Formula {
    private final T value;

    private Predicate(T value) {
        this.value = value;
    }

    protected String stringify() {
        return value.toString();
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
