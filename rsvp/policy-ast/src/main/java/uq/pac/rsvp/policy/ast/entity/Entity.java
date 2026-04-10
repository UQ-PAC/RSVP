package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.JsonObject;

import java.util.Set;
import java.util.stream.Collectors;

public class Entity {
    private final EntityReference euid;
    private final RecordValue attrs;
    private final Set<EntityReference> parents;

    Entity(JsonObject json) {
        this.euid = (EntityReference) EntityReference.deserialise(json.get("uid"));
        this.attrs = (RecordValue) EntityReference.deserialise(json.get("attrs"));
        this.parents = json.getAsJsonArray("parents").asList().stream()
                .map(j -> (EntityReference) EntityValue.deserialise(json))
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
