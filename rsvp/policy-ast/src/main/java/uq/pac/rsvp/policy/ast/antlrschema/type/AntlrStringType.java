package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrStringType extends AntlrBuiltinType {

    public AntlrStringType(SourceLoc location) {
        super(location);
    }

    public AntlrStringType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "String";
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitString(this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AntlrStringType;
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitString(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitString(this, payload);
    }
}
