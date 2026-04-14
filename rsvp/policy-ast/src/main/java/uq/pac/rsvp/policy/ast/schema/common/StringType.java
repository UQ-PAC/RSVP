package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class StringType extends CommonTypeDefinition {
    public StringType(String name, boolean required) {
        super(name, required);
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

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitString(this, payload);
    }

    @Override
    public String toString() {
        return "__cedar::String";
    }
}
