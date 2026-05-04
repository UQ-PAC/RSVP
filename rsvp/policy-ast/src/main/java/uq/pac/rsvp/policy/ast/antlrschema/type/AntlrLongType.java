package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrLongType extends AntlrBuiltinType {

    public AntlrLongType(SourceLoc location) {
        super(location);
    }

    public AntlrLongType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AntlrLongType;
    }

    @Override
    public String toString() {
        return "__cedar::Long";
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitLong(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitLong(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitLong(this, payload);
    }
}
