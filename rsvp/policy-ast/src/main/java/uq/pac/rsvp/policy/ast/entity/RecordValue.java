package uq.pac.rsvp.policy.ast.entity;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;


public class RecordValue extends EntityValue {
    private final Map<String, EntityValue> values;

    public RecordValue(Map<String, EntityValue> values) {
        this.values = Map.copyOf(values);
    }

    public RecordValue() {
        this.values = Collections.emptyMap();
    }

    public Map<String, EntityValue> getValues() {
        return values;
    }

    public EntityValue getValue(String key) {
        return values.get(key);
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

    public void forEach(BiConsumer<String, EntityValue> consumer) {
        values.forEach(consumer);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public int size() {
        return values.size();
    }

    public Set<String> keySet() {
        return values.keySet();
    }
}
