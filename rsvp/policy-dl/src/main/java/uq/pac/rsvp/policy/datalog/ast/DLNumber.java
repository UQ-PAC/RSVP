package uq.pac.rsvp.policy.datalog.ast;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public final class DLNumber extends DLTerm {
    private final double number;

    private boolean isInt() {
        return number % 1 == 0;
    }

    public DLNumber(int number) {
        this.number = number;
    }

    public DLNumber(double number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLNumber s) {
            return s.number == number;
        }
        return false;
    }

    @Override
    protected String stringify() {
        return isInt() ? Integer.toString((int) number) : Double.toString(number);
    }

    public int getInt() {
        require(isInt());
        return (int) number;
    }

    public double getNumber() {
        return number;
    }
}
