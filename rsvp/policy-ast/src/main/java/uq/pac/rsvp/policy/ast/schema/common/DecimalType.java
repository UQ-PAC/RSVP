package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DecimalType extends CommonTypeDefinition {

    public DecimalType(String name, boolean required) {
        super(name, required);
    }

    public DecimalType(boolean required) {
        super(required);
    }

    public DecimalType(String name) {
        super(name);
    }

    public DecimalType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitDecimal(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitDecimal(this);
    }
}
