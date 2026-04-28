package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
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
    public String toString() {
        return "(R) " + getName();
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
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
