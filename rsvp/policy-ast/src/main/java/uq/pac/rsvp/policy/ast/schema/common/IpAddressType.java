package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class IpAddressType extends CommonTypeDefinition {
    public IpAddressType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public IpAddressType(Map<String, String> annotations) {
        super(annotations);
    }

    public IpAddressType(boolean required) {
        super(required);
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
