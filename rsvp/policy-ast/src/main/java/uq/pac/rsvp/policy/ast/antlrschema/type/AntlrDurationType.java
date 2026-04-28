package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrDurationType extends AntlrBuiltinType {

    public AntlrDurationType(SourceLoc location) {
        super(location);
    }

    public AntlrDurationType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "duration";
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitDuration(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitDuration(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitDuration(this, payload);
    }
}
