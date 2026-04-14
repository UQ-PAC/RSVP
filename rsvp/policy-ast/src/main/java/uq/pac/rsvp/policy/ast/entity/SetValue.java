package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class SetValue extends EntityValue {
    private final Set<EntityValue> values;

    public SetValue(Set<EntityValue> values, SourceLoc location) {
        super(location);
        this.values = Set.copyOf(values);
    }

    public SetValue(Set<EntityValue> values) {
        this(values, SourceLoc.MISSING);
    }

    public Set<EntityValue> getValues() {
        return values;
    }

    public void forEach(Consumer<EntityValue> consumer) {
        values.forEach(consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof SetValue l) {
            return l.values.equals(this.values);
        }
        return false;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
