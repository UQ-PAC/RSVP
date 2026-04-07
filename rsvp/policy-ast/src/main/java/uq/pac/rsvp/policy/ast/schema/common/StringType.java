package uq.pac.rsvp.policy.ast.schema.common;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class StringType extends CommonTypeDefinition {
    public StringType(String name) {
        super(name);
    }

    public StringType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitString(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitString(this);
    }
}
