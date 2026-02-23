package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class Namespace implements SchemaFileEntry {

    private final Map<String, EntityTypeDefinition> entityTypes;
    private final Map<String, ActionDefinition> actions;
    private final Map<String, CommonTypeDefinition> commonTypes;

    // The key for this namespace within the Schema
    private String name;

    public Namespace(Map<String, EntityTypeDefinition> entityTypes, Map<String, ActionDefinition> actions,
            Map<String, CommonTypeDefinition> commonTypes) {
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

    public EntityTypeDefinition getEntityType(String name) {
        return entityTypes != null ? entityTypes.get(name) : null;
    }

    public Set<String> actionNames() {
        return actions != null ? actions.keySet() : Collections.emptySet();
    }

    public ActionDefinition getAction(String name) {
        return actions != null ? actions.get(name) : null;
    }

    public Set<String> commonTypeNames() {
        return commonTypes != null ? Set.copyOf(commonTypes.keySet()) : Collections.emptySet();
    }

    public CommonTypeDefinition getCommonType(String name) {
        return commonTypes != null ? commonTypes.get(name) : null;
    }

    public CommonTypeDefinition resolveCommonType(String name, CommonTypeDefinition definition) {
        return commonTypes != null ? commonTypes.put(name, definition) : null;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitNamespace(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitNamespace(this);
    }

}
