package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import uq.pac.rsvp.policy.ast.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitySet {
    private final Set<Entity> entities;

    public EntitySet(Set<Entity> entities) {
        this.entities = Set.copyOf(entities);
    }

    public EntitySet(String json) {
        this(JsonParser.getGson().fromJson(json, JsonArray.class));
    }

    public EntitySet(Path json) throws FileNotFoundException {
        this(JsonParser.getGson().fromJson(new FileReader(json.toFile()), JsonArray.class));
    }

    public EntitySet(JsonArray json) {
        this(json.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(Entity::new)
                .collect(Collectors.toSet()));
    }

    public static EntitySet parse(Path json) throws FileNotFoundException {
        return new EntitySet(json);
    }

    public Stream<Entity> stream() {
        return entities.stream();
    }

    public Set<Entity> getEntities() {
        return entities;
    }
}
