package uq.pac.rsvp.policy.ast.entity;


import java.util.Collections;
import java.util.Set;

public class Entity {
    private final EntityReference euid;
    private final RecordValue attrs;
    private final Set<EntityReference> parents;
    private final EntityValue context;

    public Entity(EntityReference uid, RecordValue attrs, Set<EntityReference> parents, EntityValue context) {
        this.euid = uid;
        this.attrs = attrs;
        this.parents = parents;
        this.context = context;
    }

    public Entity(EntityReference uid, RecordValue attrs, Set<EntityReference> parents) {
        this(uid, attrs, parents, new RecordValue());
    }

    public Entity(EntityReference uid, RecordValue attrs) {
        this(uid, attrs, Set.of());
    }

    public Entity(EntityReference uid) {
        this(uid, new RecordValue(Collections.emptyMap()), Set.of());
    }

    public EntityReference getEuid() {
        return euid;
    }

    public Set<EntityReference> getParents() {
        return parents;
    }

    public RecordValue getAttrs() {
        return attrs;
    }

    public EntityValue getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "{\n\tuid: " + euid + "\n\tattrs: " + attrs + "\n\tparents: " + parents + "\n}";
    }
}
