package uq.pac.rsvp.policy.ast.schema.attribute;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityOrCommonType extends AttributeType {

    private String name;
    private EntityType resolvedEntityType;
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

    public void accept(SchemaVisitor visitor) {
        visitor.visitEntityOrCommonAttributeType(this);
    }
}
