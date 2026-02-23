package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class RecordTypeDefinition extends CommonTypeDefinition {
    private final Map<String, CommonTypeDefinition> attributes;

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes, boolean required,
            Map<String, String> annotations) {
        super(required, annotations);
        this.attributes = attributes != null ? Map.copyOf(attributes) : Collections.emptyMap();
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes, Map<String, String> annotations) {
        this(attributes, false, annotations);
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes, boolean required) {
        super(required);
        this.attributes = attributes != null ? Map.copyOf(attributes) : Collections.emptyMap();
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes) {
        this(attributes, false);
    }

    public Set<String> getAttributeNames() {
        return attributes != null ? Set.copyOf(attributes.keySet()) : Collections.emptySet();
    }

    public CommonTypeDefinition getAttributeType(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    public void resolveAttributeType(String name, CommonTypeDefinition attr) {
        if (attributes != null) {
            attributes.put(name, attr);
        }
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitRecordTypeDefinition(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitRecordTypeDefinition(this);
    }
}
