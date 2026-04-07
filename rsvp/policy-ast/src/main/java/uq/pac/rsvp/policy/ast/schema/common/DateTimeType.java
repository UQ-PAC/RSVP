package uq.pac.rsvp.policy.ast.schema.common;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DateTimeType extends CommonTypeDefinition {

    public DateTimeType(String name) {
        super(name);
    }

    public DateTimeType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitDateTime(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitDateTime(this);
    }
}
