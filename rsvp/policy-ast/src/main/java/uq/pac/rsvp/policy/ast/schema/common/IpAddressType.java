package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class IpAddressType extends CommonTypeDefinition {

    public IpAddressType(String name, boolean required) {
        super(name, required);
    }

    public IpAddressType(boolean required) {
        super(required);
    }

    public IpAddressType(String name) {
        super(name);
    }

    public IpAddressType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitIpAddress(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitIpAddress(this);
    }
}
