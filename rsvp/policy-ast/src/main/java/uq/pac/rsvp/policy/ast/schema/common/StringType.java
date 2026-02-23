package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class StringType extends CommonTypeDefinition {
    public StringType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public StringType(Map<String, String> annotations) {
        super(true, annotations);
    }

    public StringType(boolean required) {
        super(required);
    }

    public StringType() {
        this(true);
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
