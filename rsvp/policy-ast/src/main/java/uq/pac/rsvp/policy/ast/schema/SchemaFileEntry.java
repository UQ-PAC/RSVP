package uq.pac.rsvp.policy.ast.schema;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public interface SchemaFileEntry {
    public void accept(SchemaVisitor visitor);

    public <T> T compute(SchemaComputationVisitor<T> visitor);
}
