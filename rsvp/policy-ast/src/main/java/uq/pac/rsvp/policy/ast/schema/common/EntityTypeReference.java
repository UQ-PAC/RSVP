package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityTypeReference extends CommonTypeDefinition {

    private final EntityTypeDefinition definition;

    public EntityTypeReference(EntityTypeDefinition definition, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.definition = definition;
    }

    public EntityTypeReference(EntityTypeDefinition definition, Map<String, String> annotations) {
        super(annotations);
        this.definition = definition;
    }

    public EntityTypeReference(EntityTypeDefinition definition, boolean required) {
        super(required);
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
}
