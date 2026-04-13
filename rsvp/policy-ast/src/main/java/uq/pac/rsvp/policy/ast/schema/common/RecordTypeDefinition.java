package uq.pac.rsvp.policy.ast.schema.common;

import java.util.*;
import java.util.stream.Collectors;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class RecordTypeDefinition extends CommonTypeDefinition {
    private final Map<String, CommonTypeDefinition> attributes;

    public RecordTypeDefinition(String name, Map<String, CommonTypeDefinition> attributes, boolean required) {
        super(name, required);
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes, boolean required) {
        super(required);
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition(String name, Map<String, CommonTypeDefinition> attributes) {
        super(name);
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes) {
        super();
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition() {
        super();
        this.attributes = Collections.emptyMap();
    }

    public Set<String> getAttributeNames() {
        return Set.copyOf(attributes.keySet());
    }

    public Map<String, CommonTypeDefinition> getAttributes() {
        return Map.copyOf(attributes);
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
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

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitRecordTypeDefinition(this, payload);
    }

    @Override
    public String toString() {
        List<String> components = new ArrayList<>();
        attributes.forEach((attr, val) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(attr);
            if (!val.isRequired()) {
                sb.append("?");
            }
            sb.append(":").append(val);
            components.add(sb.toString());
        });
        return "{ " + String.join(", ", components) + "}";
    }
}
