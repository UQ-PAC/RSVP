package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parse a JSON specification describing a set of Cedar entities and output then as an {@link EntitySet}
 */
class EntityReader {

    private static final Field POSITION;
    private static final Field LINE_START;
    private static final Field LINE_NUMBER;
    static {
        try {
            POSITION = JsonReader.class.getDeclaredField("pos");
            LINE_NUMBER = JsonReader.class.getDeclaredField("lineNumber");
            LINE_START = JsonReader.class.getDeclaredField("lineStart");
            POSITION.setAccessible(true);
            LINE_NUMBER.setAccessible(true);
            LINE_START.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int position() {
        try {
            int line = (int) LINE_NUMBER.get(reader);
            int pos = (int) POSITION.get(reader);
            int lineStart = (int) LINE_START.get(reader);
            return source.getOffset(line, pos - lineStart + 1);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final FileSource source;
    private final JsonReader reader;

    public EntityReader(Path file) throws IOException {
        this.source = new FileSource(file);
        this.reader = new JsonReader(new FileReader(file.toFile()));
    }

    private static EntityValue getReferenceOrRecord(RecordValue value) {
        AttributeName id = new AttributeName("id"),
                type = new AttributeName("type");

        if (value.size() == 2 &&
                value.attributes().equals(Set.of(type, id)) &&
                value.getValue(type) instanceof StringValue t &&
                value.getValue(id) instanceof StringValue i) {
            return new EntityReference(t.getValue(), i.getValue(), value.getLocation());
        }
        return value;
    }

    private EntityValue readEntityValue() throws IOException {
        JsonToken token = reader.peek();
        int offset = position();
        return switch (token) {
            case BEGIN_ARRAY -> {
                Set<EntityValue> values = new HashSet<>();
                reader.beginArray();
                while (reader.peek() != JsonToken.END_ARRAY) {
                    EntityValue value = readEntityValue();
                    values.add(value);
                }
                reader.endArray();
                yield new SetValue(values);
            }
            case BEGIN_OBJECT -> {
                Map<AttributeName, EntityValue> values = new HashMap<>();
                reader.beginObject();

                while (reader.peek() != JsonToken.END_OBJECT) {
                    int attrOffset = position();
                    String name = reader.nextName();
                    SourceLoc loc = new SourceLoc(source, attrOffset, name.length() + 2);
                    AttributeName an = new AttributeName(name, loc);
                    EntityValue value = readEntityValue();
                    values.put(an, value);
                }

                reader.endObject();
                reader.peek();
                System.out.println(offset + " " + position() + " " );
                SourceLoc location = new SourceLoc(source, offset, position() - offset + 1);
                RecordValue value = new RecordValue(values, location);
                AttributeName cedarEntity = new AttributeName("__entity");
                yield values.keySet().equals(Set.of(cedarEntity)) ?
                        values.get(cedarEntity) : getReferenceOrRecord(value);
            }
            case STRING -> {
                // For strings the offset is at the start, but we also need to take the account quotes
                // FIXME: if there are escaped characters, the location will likely be off
                String value = reader.nextString();
                yield new StringValue(value, new SourceLoc(source, offset, value.length() + 2));
            }
            case NUMBER -> {
                // For numbers teh location is shifted to the last digit, push it back
                long number = reader.nextLong();
                int length = Long.toString(number).length();
                yield new LongValue(number, new SourceLoc(source, offset - length + 1, length));
            }
            case BOOLEAN -> {
                // The offset for booleans will be shifted to the last char, so push it back
                boolean bool = reader.nextBoolean();
                int length = bool ? 4 : 5;
                yield new BooleanValue(bool, new SourceLoc(source, position() - length + 1, length));
            }
            case NULL -> throw new EntityException(new SourceLoc(source, offset, 4), "Null value");
            default -> throw new RuntimeException();
        };
    }

    private Entity readEntity() throws IOException {
        JsonToken token = reader.peek();
        int offset = position();
        if (token != JsonToken.BEGIN_OBJECT) {
            throw new EntityException(new SourceLoc(source, offset, 1), "Expected object start");
        }
        reader.beginObject();

        RecordValue attrs = null;
        EntityReference uid = null;
        EntityValue context = null;
        Set<EntityReference> parents = null;

        while (reader.peek() != JsonToken.END_OBJECT) {
            String name = reader.nextName();
            switch (name) {
                case "attrs" -> attrs = (RecordValue) readEntityValue();
                case "parents" -> {
                    SetValue set = (SetValue) readEntityValue();
                    parents = set.getValues().stream()
                            .map(e -> {
                                if (e instanceof EntityReference ref) {
                                    return ref;
                                }
                                throw new EntityException(e.getLocation(), "Expected entity reference");
                            })
                            .collect(Collectors.toSet());
                }
                case "context" -> context = readEntityValue();
                case "uid" -> {
                    EntityValue value = readEntityValue();
                    if (value instanceof EntityReference ref) {
                        uid = ref;
                    } else {
                        throw new EntityException(value.getLocation(), "Expected entity reference");
                    }
                }
                // FIXME: location of an attribute
                default -> throw new EntityException(new SourceLoc(source, offset, name.length()), "Unexpected entity key: " + name);
            }
        }

        SourceLoc loc = new SourceLoc(source, offset, position() - offset + 1);

        if (uid == null) {
            throw new EntityException(loc, "Missing uid entity attribute");
        }

        if (attrs == null) {
            throw new EntityException(loc, "Missing attrs entity attribute");
        }

        if (parents == null) {
            throw new EntityException(loc, "Missing parents entity attribute");
        }

        reader.endObject();
        return new Entity(uid, attrs, parents, context, loc);
    }

    private EntitySet readEntitySet() throws IOException {
        JsonToken token = reader.peek();
        if (token != JsonToken.BEGIN_ARRAY) {
            throw new EntityException(new SourceLoc(source, position(), 1), "Expected array start");
        }
        reader.beginArray();

        Set<Entity> entities = new HashSet<>();
        while (reader.peek() != JsonToken.END_ARRAY) {
            Entity entity = readEntity();
            entities.add(entity);
        }

        reader.endArray();
        return new EntitySet(entities);
    }

    public EntitySet parse() throws IOException, IllegalAccessException {
        return readEntitySet();
    }
}
