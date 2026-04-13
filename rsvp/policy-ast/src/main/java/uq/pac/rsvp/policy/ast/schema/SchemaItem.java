package uq.pac.rsvp.policy.ast.schema;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public interface SchemaItem {
    public void accept(SchemaVisitor visitor);

    public <T> T compute(SchemaComputationVisitor<T> visitor);

    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload);
}
