package uq.pac.rsvp.policy.datalog.invariant;

public enum Quantifier {
    ALL("all"),
    SOME("some"),
    NONE("none");

    Quantifier(String quantifier) {
        this.value = quantifier;
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
