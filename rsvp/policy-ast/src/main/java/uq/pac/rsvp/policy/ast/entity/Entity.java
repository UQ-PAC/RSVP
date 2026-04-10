package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class Entity {
    private final EntityReference euid;
    private final RecordValue attrs;
    private final Set<EntityReference> parents;

    public Entity(EntityReference uid, RecordValue attrs, Set<EntityReference> parents) {
        this.euid = uid;
        this.attrs = attrs;
        this.parents = parents;
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
        this.euid = (EntityReference) EntityReference.deserialise(json.get("uid"));
        this.attrs = (RecordValue) EntityReference.deserialise(json.get("attrs"));
        this.parents = json.get("parents").getAsJsonArray().asList().stream()
                .map(j -> (EntityReference) EntityValue.deserialise(j))
                .collect(Collectors.toUnmodifiableSet());
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

    @Override
    public String toString() {
        return "{\n\tuid: " + euid + "\n\tattrs: " + attrs + "\n\tparents: " + parents + "\n}";
    }

}
