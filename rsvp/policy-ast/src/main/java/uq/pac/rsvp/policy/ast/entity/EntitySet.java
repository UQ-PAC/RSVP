/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.JsonArray;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public class EntitySet {
    private final Set<Entity> entities;

    public EntitySet(Set<Entity> entities) {
        this.entities = Set.copyOf(entities);
    }

    public static EntitySet parse(Path json) throws IOException {
        return EntitySetParser.parse(json);
    }

    public static EntitySet parse(String filename, String json) {
        return EntitySetParser.parse(filename, json);
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

    public JsonArray toJson() {
        JsonArray array = new JsonArray();
        entities.forEach(v -> array.add(v.toJson()));
        return array;
    }
}
