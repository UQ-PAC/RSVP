package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

import java.util.Objects;

public class LongValue extends EntityValue {
    private final long value;

    public LongValue(long value, SourceLoc location) {
        super(location);
        this.value = value;
    }

    public LongValue(long value) {
        this(value, SourceLoc.MISSING);
    }

    public long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof LongValue l) {
            return l.value == this.value;
        }
        return false;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
