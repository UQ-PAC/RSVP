package uq.pac.rsvp.policy.ast.entity;

import java.util.Map;
import java.util.Objects;


public class RecordValue extends EntityValue {
    private final Map<String, EntityValue> values;

    public RecordValue(Map<String, EntityValue> values) {
        this.values = Map.copyOf(values);
    }

    public Map<String, EntityValue> getValues() {
        return values;
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
        } else if (other instanceof RecordValue l) {
            return l.values.equals(this.values);
        }
        return false;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
