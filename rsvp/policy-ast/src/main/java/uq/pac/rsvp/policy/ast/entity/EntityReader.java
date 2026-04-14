package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class EntityReader {
    // FIXME: Need source locations

    static Field POSITION;
    static {
        try {
            POSITION = JsonReader.class.getDeclaredField("pos");
            POSITION.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int position(JsonReader reader) {
        try {
            return (int) POSITION.get(reader);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static EntityValue getReferenceOrRecord(RecordValue value) {
        if (value.size() == 2 &&
                value.keySet().equals(Set.of("type", "id")) &&
                value.getValue("type") instanceof StringValue type &&
                value.getValue("id") instanceof StringValue id) {
            return new EntityReference(type.getValue(), id.getValue());
        }
        return value;
    }

    private static EntityValue readEntityValue(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> {
                Set<EntityValue> values = new HashSet<>();
                reader.beginArray();
                while (reader.peek() != JsonToken.END_ARRAY) {
                    EntityValue value = readEntityValue(reader);
                    values.add(value);
                }
                reader.endArray();
                yield new SetValue(values);
            }
            case BEGIN_OBJECT -> {
                Map<String, EntityValue> values = new HashMap<>();
                reader.beginObject();
                while (reader.peek() != JsonToken.END_OBJECT) {
                    String name = reader.nextName();
                    EntityValue value = readEntityValue(reader);
                    values.put(name, value);
                }
                reader.endObject();
                RecordValue value = new RecordValue(values);
                yield values.keySet().equals(Set.of("__entity")) ?
                        values.get("__entity") : getReferenceOrRecord(value);
            }
            case STRING -> new StringValue(reader.nextString());
            case NUMBER -> new LongValue(reader.nextLong());
            case BOOLEAN -> new BooleanValue(reader.nextBoolean());
            case NULL -> throw new Error("Null value");
            default -> throw new RuntimeException();
        };
    }

    static Entity readEntity(JsonReader reader) throws IOException, IllegalAccessException {
        JsonToken token = reader.peek();
        if (token != JsonToken.BEGIN_OBJECT) {
            throw new Error("Expected object start");
        }
        reader.beginObject();

        RecordValue attrs = null;
        EntityReference uid = null;
        EntityValue context = null;
        Set<EntityReference> parents = null;

        while (reader.peek() != JsonToken.END_OBJECT) {
            String name = reader.nextName();
            switch (name) {
                case "attrs" -> attrs = (RecordValue) readEntityValue(reader);
                case "parents" -> {
                    SetValue set = (SetValue) readEntityValue(reader);
                    parents = set.getValues().stream()
                            .map(e -> (EntityReference) e)
                            .collect(Collectors.toSet());
                }
                case "context" -> context = readEntityValue(reader);
                case "uid" -> uid = (EntityReference) readEntityValue(reader);
                default -> throw new Error("Unexpected key: " + name);
            }
        }
        reader.endObject();
        return new Entity(uid, attrs, parents, context);
    }

    static EntitySet readEntitySet (JsonReader reader) throws IOException, IllegalAccessException {
        JsonToken token = reader.peek();
        if (token != JsonToken.BEGIN_ARRAY) {
            throw new Error("Expected object start");
        }
        reader.beginArray();

        Set<Entity> entities = new HashSet<>();
        while (reader.peek() != JsonToken.END_ARRAY) {
            Entity entity = readEntity(reader);
            entities.add(entity);
        }

        reader.endArray();
        return new EntitySet(entities);
    }

    static EntitySet parse(Path json) throws IOException, IllegalAccessException {
        return readEntitySet(new JsonReader(new FileReader(json.toFile())));
    }
}
