package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

public class BooleanValue extends EntityValue {
    private final boolean value;

    public BooleanValue(boolean value, SourceLoc location) {
        super(location);
        this.value = value;
    }

    public BooleanValue(boolean value) {
        this(value, SourceLoc.MISSING);
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value ? 0 : 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof BooleanValue l) {
            return l.value == this.value;
        }
        return false;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
