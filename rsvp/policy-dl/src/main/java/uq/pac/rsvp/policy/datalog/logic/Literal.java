package uq.pac.rsvp.policy.datalog.logic;

public class Literal extends Formula {
    private final boolean value;

    private Literal(boolean value) {
        this.value = value;
    }

    private static final Literal TRUE = new Literal(true);
    private static final Literal FALSE = new Literal(false);

    protected String stringify() {
        return Boolean.toString(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } if (other == this) {
            return true;
        } else if (other instanceof Literal lit) {
            return this.value == lit.value;
        }
        return false;
    }

    public boolean asBoolean() {
        return value;
    }
}
