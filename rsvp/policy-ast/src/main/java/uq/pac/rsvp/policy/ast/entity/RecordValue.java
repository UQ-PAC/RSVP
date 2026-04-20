package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;


public class RecordValue extends EntityValue {
    private final Map<AttributeName, EntityValue> values;

    public RecordValue(Map<AttributeName, EntityValue> values, SourceLoc location) {
        super(location);
        this.values = Map.copyOf(values);
    }

    public RecordValue(Map<AttributeName, EntityValue> values) {
        this(values, SourceLoc.MISSING);
    }

    public RecordValue() {
        this(Collections.emptyMap(), SourceLoc.MISSING);
    }

    public Map<AttributeName, EntityValue> getValues() {
        return values;
    }

    public EntityValue getValue(AttributeName key) {
        return values.get(key);
    }

    public EntityValue getValue(String key) {
        return getValue(new AttributeName(key));
    }

    public AttributeName getAttributeName(String attribute) {
        return values.keySet().stream()
                .filter(a -> a.getValue().equals(attribute))
                .findFirst()
                .orElse(null);
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

    public void forEach(BiConsumer<AttributeName, EntityValue> consumer) {
        values.forEach(consumer);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public int size() {
        return values.size();
    }

    public Set<AttributeName> attributes() {
        return values.keySet();
    }
}
