package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class Namespace {

    private final Map<String, EntityType> entityTypes;
    private final Map<String, Action> actions;
    private final Map<String, AttributeType> commonTypes;

    // The key for this namespace within the Schema
    private String name;

    public Namespace(Map<String, EntityType> entityTypes, Map<String, Action> actions,
            Map<String, AttributeType> commonTypes) {
        this.entityTypes = entityTypes != null ? Map.copyOf(entityTypes) : Collections.emptyMap();
        this.actions = actions != null ? Map.copyOf(actions) : Collections.emptyMap();
        this.commonTypes = commonTypes != null ? Map.copyOf(commonTypes) : Collections.emptyMap();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> entityTypeNames() {
        return entityTypes != null ? entityTypes.keySet() : Collections.emptySet();
    }

    public EntityType getEntityType(String name) {
        return entityTypes != null ? entityTypes.get(name) : null;
    }

    public Set<String> actionNames() {
        return actions != null ? actions.keySet() : Collections.emptySet();
    }

    public Action getAction(String name) {
        return actions != null ? actions.get(name) : null;
    }

    public Set<String> commonTypeNames() {
        return commonTypes != null ? commonTypes.keySet() : Collections.emptySet();
    }

    public AttributeType getCommonType(String name) {
        return commonTypes != null ? commonTypes.get(name) : null;
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visitNamespace(this);
    }

    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitNamespace(this);
    }

}
