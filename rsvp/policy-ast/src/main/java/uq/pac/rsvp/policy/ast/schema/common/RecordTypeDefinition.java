package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Collections;
import java.util.HashMap;
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
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes, Map<String, String> annotations) {
        super(annotations);
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes, boolean required) {
        this(attributes, required, null);
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes) {
        this(attributes, null);
    }

    public RecordTypeDefinition() {
        this(null, false, null);
    }

    public Set<String> getAttributeNames() {
        return Set.copyOf(attributes.keySet());
    }

    public CommonTypeDefinition getAttributeType(String name) {
        return attributes.get(name);
    }

    public void resolveAttributeType(String name, CommonTypeDefinition attr) {
        attributes.put(name, attr);
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
