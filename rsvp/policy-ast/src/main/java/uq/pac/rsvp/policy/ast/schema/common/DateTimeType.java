package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DateTimeType extends CommonTypeDefinition {

    public DateTimeType(String name, boolean required) {
        super(name, required);
    }

    public DateTimeType(boolean required, Map<String, String> annotations) {
        super(required);
    }

    public DateTimeType(String name) {
        super(name);
    }

    public DateTimeType(boolean required) {
        super(required);
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
