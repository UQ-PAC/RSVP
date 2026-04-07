package uq.pac.rsvp.policy.ast.schema.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class RecordTypeDefinition extends CommonTypeDefinition {

    public static class Attribute {
        private final boolean required;
        private final String name;

        public Attribute(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        public Attribute(String name) {
            this(name, true);
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other instanceof Attribute a) {
                return a.name.equals(this.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private final Map<String, CommonTypeDefinition> attributes;

    public RecordTypeDefinition(String name, Map<String, CommonTypeDefinition> attributes, boolean required) {
        super(name, required);
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }


    public RecordTypeDefinition(String name, Map<String, CommonTypeDefinition> attributes) {
        this(name, attributes, false);
    }

    public RecordTypeDefinition(Map<String, CommonTypeDefinition> attributes) {
        this(null, attributes);
    }

    public RecordTypeDefinition() {
        this(null, null, false);
    }

    public Set<String> getAttributeNames() {
        return Set.copyOf(attributes.keySet());
    }

    public Map<String, CommonTypeDefinition> getAttributes() {
        return Map.copyOf(attributes);
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
