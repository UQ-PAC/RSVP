package uq.pac.rsvp.policy.ast.schema.attribute;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityOrCommonType extends AttributeType {

    private final String name;

    // Resolved entity type referenced by this type (if applicable)
    private EntityType resolvedEntityType;

    // Resolved common type referenced by this type (if applicable)
    private AttributeType resolvedAttributeType;

    public EntityOrCommonType(String name, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.name = name;
    }

    public EntityOrCommonType(String name, Map<String, String> annotations) {
        super(false, annotations);
        this.name = name;
    }

    public EntityOrCommonType(String name, boolean required) {
        super(required);
        this.name = name;
    }

    public EntityOrCommonType(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityTypeOrNull() {
        return resolvedEntityType;
    }

    public AttributeType getCommonTypeOrNull() {
        return resolvedAttributeType;
    }

    public boolean isResolved() {
        return resolvedEntityType != null || resolvedAttributeType != null;
    }

    public void resolve(Schema schema, Namespace local) {
        EntityType entity = Schema.resolveEntityType(name, schema, local);

        if (entity != null) {
            resolvedEntityType = entity;
        } else {
            AttributeType attr = Schema.resolveCommonType(name, schema, local);

            if (attr != null) {
                resolvedAttributeType = attr;
            }
        }
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitEntityOrCommonAttributeType(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitEntityOrCommonAttributeType(this);
    }
}
