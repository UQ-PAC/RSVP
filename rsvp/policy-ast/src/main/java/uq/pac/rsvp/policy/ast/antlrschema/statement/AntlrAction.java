package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrAction extends AntlrSchemaStatement {

    public AntlrAction(String namespace, String name, SourceLoc location) {
        super(namespace, name, location);
    }

    @Override
    public String toString() {
        return "action " + getName() + ";";
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitAction(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitAction(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitAction(this, payload);
    }
}
