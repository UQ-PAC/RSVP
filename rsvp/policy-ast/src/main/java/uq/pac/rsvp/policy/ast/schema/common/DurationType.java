package uq.pac.rsvp.policy.ast.schema.common;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DurationType extends CommonTypeDefinition {

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
