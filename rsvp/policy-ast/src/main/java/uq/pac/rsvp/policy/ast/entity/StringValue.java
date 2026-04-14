package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

import java.util.Objects;

public class StringValue extends EntityValue {
    private final String value;

    public StringValue(String value, SourceLoc location) {
        super(location);
        this.value = value;
    }

    public StringValue(String value) {
        this(value, SourceLoc.MISSING);
    }

    public String getValue() {
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
        } else if (other instanceof StringValue l) {
            return l.value.equals(this.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return value;
    }
}
