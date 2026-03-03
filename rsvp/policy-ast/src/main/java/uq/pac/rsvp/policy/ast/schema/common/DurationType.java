package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class DurationType extends CommonTypeDefinition {

    public DurationType(String name, boolean required, Map<String, String> annotations) {
        super(name, required, annotations);
    }

    public DurationType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public DurationType(String name, Map<String, String> annotations) {
        super(name, annotations);
    }

    public DurationType(Map<String, String> annotations) {
        super(annotations);
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
