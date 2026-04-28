package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrIpAddressType extends AntlrBuiltinType {

    public AntlrIpAddressType(SourceLoc location) {
        super(location);
    }

    public AntlrIpAddressType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "ipaddr";
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitIpAddress(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitIpAddress(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitIpAddress(this, payload);
    }
}
