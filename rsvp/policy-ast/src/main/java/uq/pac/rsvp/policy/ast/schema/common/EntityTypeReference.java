package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityTypeReference extends CommonTypeDefinition {

    private final EntityTypeDefinition definition;

    public EntityTypeReference(String name, EntityTypeDefinition definition, boolean required) {
        super(name, required);
        this.definition = definition;
    }

    public EntityTypeReference(EntityTypeDefinition definition, boolean required) {
        super(required);
        this.definition = definition;
    }

    public EntityTypeReference(String name, EntityTypeDefinition definition) {
        super(name);
        this.definition = definition;
    }

    public EntityTypeReference(EntityTypeDefinition definition) {
        super();
        this.definition = definition;
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

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitEntityTypeReference(this, payload);
    }

    @Override
    public String toString() {
        return definition.getName();
    }
}
