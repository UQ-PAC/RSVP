package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Map;

public class AntlrRecordType extends AntlrBuiltinType {

    public static class Attribute {
        public final String name;
        public final boolean required;

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
        public String toString() {
            return name + (required ? "" : "?");
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other == null) {
                return false;
            } else if (other instanceof Attribute attr) {
                return attr.name.equals(this.name);
            }
            return false;
        }
    }

    private final Map<Attribute, AntlrBuiltinType> attributes;

    public AntlrRecordType(Map<Attribute, AntlrBuiltinType> attributes, SourceLoc location) {
        super(location);
        this.attributes = Map.copyOf(attributes);
    }

    public Map<Attribute, AntlrBuiltinType> getAttributes() {
        return attributes;
    }

    public AntlrBuiltinType getAttribute(String attr) {
        return attributes.get(new Attribute(attr));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\n");
        attributes.forEach((attr, type) -> {
            sb.append("    ")
                    .append('"')
                    .append(attr.toString())
                    .append('"')
                    .append(": ")
                    .append(type.toString())
                    .append(",\n");
        });
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitRecord(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitRecord(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitRecord(this, payload);
    }
}
