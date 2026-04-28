package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrTypeReference extends AntlrBuiltinType {

    private final String name;
    private final String namespace;

    public AntlrTypeReference(String namespace, String name, SourceLoc location) {
        super(location);
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return namespace.isEmpty() ? name : namespace + "::" + name;
    }

    public String getBaseName() {
        return name;
    }

    public String getNamespace() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AntlrTypeReference ref) {
            return ref.namespace.equals(this.namespace) &&
                    ref.name.equals(this.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "(R) " + getName();
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitReference(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitReference(this, payload);
    }
}
