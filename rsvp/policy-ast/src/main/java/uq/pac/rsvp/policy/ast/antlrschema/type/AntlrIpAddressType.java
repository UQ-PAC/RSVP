package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
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
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitIpAddress(this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IpAddressType;
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
