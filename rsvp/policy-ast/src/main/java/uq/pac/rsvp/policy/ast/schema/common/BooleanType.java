package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class BooleanType extends CommonTypeDefinition {

    public BooleanType(String name, boolean required) {
        super(name, required);
    }

    public BooleanType(boolean required) {
        super(required);
    }

    public BooleanType(String name) {
        super(name);
    }

    public BooleanType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitBoolean(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitBoolean(this);
    }
}
