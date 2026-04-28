package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Set;

public abstract class AntlrEnumEntityType extends AntlrEntityType {
    private final Set<String> names;

    public AntlrEnumEntityType(String namespace, String name, Set<String> names, SourceLoc location) {
        super(namespace, name, location);
        this.names = Set.copyOf(names);
    }

    public Set<String> getEnumNames() {
        return names;
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitEnumEntity(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitEnumEntity(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitEnumEntity(this, payload);
    }
}
