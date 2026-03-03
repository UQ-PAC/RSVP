package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class StringType extends CommonTypeDefinition {
    public StringType(String name, boolean required, Map<String, String> annotations) {
        super(name, required, annotations);
    }

    public StringType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public StringType(String name, Map<String, String> annotations) {
        super(name, annotations);
    }

    public StringType(Map<String, String> annotations) {
        super(annotations);
    }

    public StringType(boolean required) {
        super(required);
    }

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
