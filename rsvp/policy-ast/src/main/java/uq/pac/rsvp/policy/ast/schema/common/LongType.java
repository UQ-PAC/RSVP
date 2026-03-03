package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class LongType extends CommonTypeDefinition {

    public LongType(String name, boolean required, Map<String, String> annotations) {
        super(name, required, annotations);
    }

    public LongType(boolean required, Map<String, String> annotations) {
        super(required, annotations);
    }

    public LongType(String name, Map<String, String> annotations) {
        super(name, annotations);
    }

    public LongType(Map<String, String> annotations) {
        super(annotations);
    }

    public LongType(boolean required) {
        super(required);
    }

    public LongType(String name) {
        super(name);
    }

    public LongType() {
        super();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitLong(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitLong(this);
    }
}
