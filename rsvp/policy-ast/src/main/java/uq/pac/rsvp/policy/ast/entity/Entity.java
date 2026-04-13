package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

    Entity(JsonObject json) {
        // FIXME: Entities are specified by a user and we can expect mistakes
        //        Need better error reporting here
        // FIXME: Move into another deserialiser
        this.euid = (EntityReference) EntityReference.deserialise(json.get("uid"));
        this.attrs = (RecordValue) EntityReference.deserialise(json.get("attrs"));
        this.parents = json.get("parents").getAsJsonArray().asList().stream()
                .map(j -> (EntityReference) EntityValue.deserialise(j))
                .collect(Collectors.toUnmodifiableSet());
        this.context = json.has("context") ?
                EntityReference.deserialise(json.get("context")) : new RecordValue();
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
