package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DateTimeType extends CommonTypeDefinition {

    public DateTimeType(String name, boolean required, Map<String, String> annotations) {
        super(name, required, annotations);
    }

    public DateTimeType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public DateTimeType(String name, Map<String, String> annotations) {
        super(name, annotations);
    }

    public DateTimeType(Map<String, String> annotations) {
        super(annotations);
    }

    public DateTimeType(boolean required) {
        super(required);
    }

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
