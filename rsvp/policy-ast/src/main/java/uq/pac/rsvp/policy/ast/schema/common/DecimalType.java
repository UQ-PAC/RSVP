package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DecimalType extends CommonTypeDefinition {
    public DecimalType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public DecimalType(Map<String, String> annotations) {
        super(annotations);
    }

    public DecimalType(boolean required) {
        super(required);
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
