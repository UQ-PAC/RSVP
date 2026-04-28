package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrBooleanType extends AntlrBuiltinType {

    public AntlrBooleanType(SourceLoc location) {
        super(location);
    }

    public AntlrBooleanType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "Bool";
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitBoolean(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitBoolean(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitBoolean(this, payload);
    }
}
