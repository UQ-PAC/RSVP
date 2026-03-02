package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Namespace {

    private final Map<String, EntityTypeDefinition> entityTypes;
    private final Map<String, ActionDefinition> actions;
    private final Map<String, CommonTypeDefinition> commonTypes;

    // The key for this namespace within the Schema
    private String name;

    public Namespace(Map<String, EntityTypeDefinition> entityTypes, Map<String, ActionDefinition> actions,
            Map<String, CommonTypeDefinition> commonTypes) {
        this.entityTypes = entityTypes != null ? new HashMap<>(entityTypes) : new HashMap<>();
        this.actions = actions != null ? new HashMap<>(actions) : new HashMap<>();
        this.commonTypes = commonTypes != null ? new HashMap<>(commonTypes) : new HashMap<>();
    }

    public Namespace(Map<String, EntityTypeDefinition> entityTypes, Map<String, ActionDefinition> actions) {
        this(entityTypes, actions, null);
    }

    public Namespace() {
        this(null, null, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> entityTypeNames() {
        return entityTypes.keySet();
    }

    public EntityTypeDefinition getEntityType(String name) {
        return entityTypes.get(name);
    }

    public Set<String> actionNames() {
        return actions.keySet();
    }

    public ActionDefinition getAction(String name) {
        return actions.get(name);
    }

    public Set<String> commonTypeNames() {
        return Set.copyOf(commonTypes.keySet());
    }

    public CommonTypeDefinition getCommonType(String name) {
        return commonTypes.get(name);
    }

    public void resolveCommonType(String name, CommonTypeDefinition definition) {
        commonTypes.put(name, definition);
    }

}
