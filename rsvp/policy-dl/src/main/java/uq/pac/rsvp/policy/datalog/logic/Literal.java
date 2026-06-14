package uq.pac.rsvp.policy.datalog.logic;

public class Literal extends Formula {
    private final boolean value;

    private Literal(boolean value) {
        this.value = value;
    }

    public static final Literal TRUE = new Literal(true);
    public static final Literal FALSE = new Literal(false);

    protected String stringify() {
        return Boolean.toString(value);
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor) {
        return visitor.visitLiteral(this);
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
