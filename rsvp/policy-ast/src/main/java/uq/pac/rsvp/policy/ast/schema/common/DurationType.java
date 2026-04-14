package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DurationType extends CommonTypeDefinition {

    public DurationType(String name, boolean required) {
        super(name, required);
    }

    public DurationType(boolean required) {
        super(required);
    }

    public DurationType(String name) {
        super(name);
    }

    public DurationType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitDuration(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitDuration(this);
    }
}
