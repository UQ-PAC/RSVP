package uq.pac.rsvp.policy.ast.schema.common;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityTypeReference extends CommonTypeDefinition {

    private final EntityTypeDefinition definition;

    public EntityTypeReference(String name, EntityTypeDefinition definition) {
        super(name);
        this.definition = definition;
    }

    public EntityTypeReference(EntityTypeDefinition definition) {
        this(null, definition);
    }

    public EntityTypeReference() {
        this(null, null);
    }

    public EntityTypeDefinition getDefinition() {
        return definition;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitEntityTypeReference(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitEntityTypeReference(this);
    }
}
