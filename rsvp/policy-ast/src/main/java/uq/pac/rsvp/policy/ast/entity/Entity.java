package uq.pac.rsvp.policy.ast.entity;


import uq.pac.rsvp.support.SourceLoc;

import java.util.Collections;
import java.util.Set;

public class Entity {
    private final SourceLoc location;
    private final EntityReference euid;
    private final RecordValue attrs;
    private final Set<EntityReference> parents;
    private final EntityValue context;

    public Entity(EntityReference uid, RecordValue attrs, Set<EntityReference> parents, EntityValue context, SourceLoc location) {
        this.euid = uid;
        this.attrs = attrs;
        this.parents = parents;
        this.context = context;
        this.location = location;
    }

    public Entity(EntityReference uid, RecordValue attrs, Set<EntityReference> parents, EntityValue context) {
        this(uid, attrs, parents, context, SourceLoc.MISSING);
    }

    public Entity(EntityReference euid, SourceLoc location) {
        this(euid, new RecordValue(), Collections.emptySet(), null, location);
    }

    public Entity(EntityReference euid) {
        this(euid, SourceLoc.MISSING);
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

    public SourceLoc getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "{\n\tuid: " + euid + "\n\tattrs: " + attrs + "\n\tparents: " + parents + "\n}";
    }
}
