package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DecimalType extends CommonTypeDefinition {

    public DecimalType(String name, boolean required, Map<String, String> annotations) {
        super(name, required, annotations);
    }

    public DecimalType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public DecimalType(String name, Map<String, String> annotations) {
        super(name, annotations);
    }

    public DecimalType(Map<String, String> annotations) {
        super(annotations);
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
