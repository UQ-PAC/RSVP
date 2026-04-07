package uq.pac.rsvp.policy.ast.schema.common;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;
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

        private Attribute() {
            this(null, true);
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

        @Override
        public String toString() {
            return name + (required ? "" : "?");
        }
    }

    private final Map<Attribute, CommonTypeDefinition> attributes;

    public RecordTypeDefinition(String name, Map<Attribute, CommonTypeDefinition> attributes) {
        super(name);
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public RecordTypeDefinition(Map<Attribute, CommonTypeDefinition> attributes) {
        this(null, attributes);
    }

    public RecordTypeDefinition() {
        this(null, null);
    }

    public Map<Attribute, CommonTypeDefinition> getAttributes() {
        return Map.copyOf(attributes);
    }

    public CommonTypeDefinition getAttributeType(Attribute attr) {
        return attributes.get(attr);
    }

    public CommonTypeDefinition getAttributeType(String attr) {
        return attributes.get(new Attribute(attr));
    }

    public void resolveAttributeType(Attribute name, CommonTypeDefinition attr) {
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

    public static class Builder {
        private String name;
        private final Map<Attribute, CommonTypeDefinition> attributes;

        public Builder() {
            name = null;
            attributes = new HashMap<>();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder attribute(String name, boolean required, CommonTypeDefinition val) {
            attributes.put(new Attribute(name, required), val);
            return this;
        }

        public Builder attribute(String name, CommonTypeDefinition val) {
            return attribute(name, true, val);
        }

        public RecordTypeDefinition build() {
            return new RecordTypeDefinition(name, attributes);
        }
    }

}
