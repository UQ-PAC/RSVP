package uq.pac.rsvp.policy.ast.schema.attribute;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class RecordType extends AttributeType {
    private final Map<String, AttributeType> attributes;

    public RecordType(Map<String, AttributeType> attributes, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.attributes = attributes != null ? Map.copyOf(attributes) : Collections.emptyMap();
    }

    public RecordType(Map<String, AttributeType> attributes, Map<String, String> annotations) {
        this(attributes, false, annotations);
    }

    public RecordType(Map<String, AttributeType> attributes, boolean required) {
        super(required);
        this.attributes = attributes != null ? Map.copyOf(attributes) : Collections.emptyMap();
    }

    public RecordType(Map<String, AttributeType> attributes) {
        this(attributes, false);
    }

    public Set<String> getAttributeNames() {
        return attributes != null ? attributes.keySet() : Collections.emptySet();
    }

    public AttributeType getAttributeType(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitRecordAttributeType(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitRecordAttributeType(this);
    }
}
