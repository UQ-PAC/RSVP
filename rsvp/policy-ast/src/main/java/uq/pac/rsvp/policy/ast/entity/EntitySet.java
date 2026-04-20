package uq.pac.rsvp.policy.ast.entity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public class EntitySet {
    private final Set<Entity> entities;

    public EntitySet(Set<Entity> entities) {
        this.entities = Set.copyOf(entities);
    }

    public static EntitySet parse(Path json) throws IOException, IllegalAccessException {
        return new EntityReader(json).parse();
    }

    public static EntitySet parse(String filename, String json) throws IOException, IllegalAccessException {
        return new EntityReader(filename, json).parse();
    }

    public Stream<Entity> stream() {
        return entities.stream();
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    @Override
    public String toString() {
        return entities.toString();
    }
}
