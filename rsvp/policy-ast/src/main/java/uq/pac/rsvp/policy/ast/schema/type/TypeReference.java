package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.parser.TypeReferenceParser;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Objects;

public class TypeReference extends BuiltinType {

    private final String name;
    private final String namespace;

    public TypeReference(String namespace, String name, SourceLoc location) {
        super(location);
        this.name = name;
        this.namespace = namespace;
    }

    public static TypeReference parse(String text) {
        return TypeReferenceParser.parse(text.trim());
    }

    public TypeReference(String namespace, String name) {
        this(namespace, name, SourceLoc.MISSING);
    }

    public String getName() {
        String prefix = "";
        if (namespace == null) {
            prefix = "???::";
        } else if (!namespace.isEmpty()) {
            prefix = namespace + "::";
        }
        return prefix + name;
    }

    public String getBaseName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof TypeReference ref) {
            return Objects.equals(ref.namespace, this.namespace) &&
                    ref.name.equals(this.name);
        }
        return false;
    }

    public TypeReference with(SourceLoc location) {
        return new TypeReference(namespace, name, location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitTypeReference(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitTypeReference(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitTypeReference(this, payload);
    }
}
