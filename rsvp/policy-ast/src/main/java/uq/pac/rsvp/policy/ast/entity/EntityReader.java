package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
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
            return source.getPosition(line + 1, pos - lineStart);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private enum EntityAttribute {
        ATTRS("attrs", true),
        UID("uid", true),
        PARENTS("parents", true),
        CONTEXT("context", false);

        private final String name;
        private final boolean required;

        EntityAttribute(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        private static final Map<Class<?>, String> LABELS = Map.of(
                EntityReference.class, "entity reference",
                RecordValue.class, "record",
                SetValue.class, "set");

        private static final Set<String> ATTRIBUTES = new HashSet<>();
        static {
            for (EntityAttribute attr : EntityAttribute.values()) {
                ATTRIBUTES.add(attr.name);
            }
        }

        public static boolean contains(String attr) {
            return ATTRIBUTES.contains(attr);
        }

        @SuppressWarnings("unchecked")
        <E extends EntityValue> E get(RecordValue entityRecord, Class<E> target) {
            E val = (E) entityRecord.getValue(name);

            if (!required && val == null) {
                return null;
            }
            if (val == null) {
                throw new EntityException(entityRecord.getLocation(), "Missing " + name + " entity attribute");
            }
            if (target.isInstance(val)) {
                return target.cast(val);
            } else {
                throw new EntityException(val.getLocation(), "Expected " + LABELS.get(target));
            }
        }
    }

    private final FileSource source;
    private final JsonReader reader;

    private SourceLoc loc(int offset, int length) {
        return new SourceLoc(source, offset, length);
    }

    EntityReader(Path file) throws IOException {
        this.source = new FileSource(file);
        this.reader = new JsonReader(new FileReader(file.toFile()));
    }

    EntityReader(String filename, String json) {
        this.source = new FileSource(filename, json);
        this.reader = new JsonReader(new StringReader(json));
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
        int offset = position() - 1;
        return switch (token) {
            case BEGIN_ARRAY -> {
                Set<EntityValue> values = new HashSet<>();
                reader.beginArray();
                while (reader.peek() != JsonToken.END_ARRAY) {
                    values.add(readEntityValue());
                }
                reader.endArray();
                yield new SetValue(values, loc(offset, position() - offset));
            }
            case BEGIN_OBJECT -> {
                Map<AttributeName, EntityValue> values = new HashMap<>();
                reader.beginObject();

                while (reader.peek() != JsonToken.END_OBJECT) {
                    int attrPosition = position() - 1;
                    String name = reader.nextName();
                    SourceLoc loc = loc(attrPosition,name.length() + 2);
                    AttributeName attrName = new AttributeName(name, loc);
                    EntityValue value = readEntityValue();
                    values.put(attrName, value);
                }

                reader.endObject();
                SourceLoc location = loc(offset, position() - offset);
                RecordValue value = new RecordValue(values, location);
                AttributeName cedarEntity = new AttributeName("__entity");
                yield values.keySet().equals(Set.of(cedarEntity)) ?
                        values.get(cedarEntity) : getReferenceOrRecord(value);
            }
            case STRING -> {
                // For strings the offset is at the start, but we also need to take the account quotes
                // FIXME: if there are escaped characters, the location will likely be off
                String value = reader.nextString();
                yield new StringValue(value, loc(offset, value.length() + 2));
            }
            case NUMBER -> {
                // For numbers the location is shifted to the last digit, push it back
                long number = reader.nextLong();
                int length = Long.toString(number).length();
                yield new LongValue(number, loc(offset - length + 1, length));
            }
            case BOOLEAN -> {
                // The offset for booleans will be shifted to the last char, so push it back
                boolean bool = reader.nextBoolean();
                int length = bool ? 4 : 5;
                yield new BooleanValue(bool, loc(position() - length, length));
            }
            case NULL -> throw new EntityException(loc(offset, 4), "Null value");
            default -> throw new RuntimeException();
        };
    }

    private Entity readEntity() throws IOException {
        JsonToken token = reader.peek();
        int offset = position() - 1;
        if (token != JsonToken.BEGIN_OBJECT) {
            throw new EntityException(loc(offset, 1), "Expected object start");
        }
        RecordValue entityRecord = (RecordValue) readEntityValue();

        EntityReference uid = EntityAttribute.UID.get(entityRecord, EntityReference.class);
        RecordValue attrs = EntityAttribute.ATTRS.get(entityRecord, RecordValue.class);
        EntityValue context = EntityAttribute.CONTEXT.get(entityRecord, EntityValue.class);
        SetValue parentsSet = EntityAttribute.PARENTS.get(entityRecord, SetValue.class);
        Set<EntityReference>  parents = parentsSet.getValues().stream()
                .map(e -> {
                    if (e instanceof EntityReference ref) {
                        return ref;
                    }
                    throw new EntityException(e.getLocation(), "Expected entity reference");
                })
                .collect(Collectors.toSet());

        entityRecord.forEach(((attr, value) -> {
            if (!EntityAttribute.contains(attr.getValue())) {
                throw new EntityException(attr.getLocation(), "Unexpected entity key: " + attr);
            }
        }));

        return new Entity(uid, attrs, parents, context, entityRecord.getLocation());
    }

    private EntitySet readEntitySet() throws IOException {
        JsonToken token = reader.peek();
        if (token != JsonToken.BEGIN_ARRAY) {
            throw new EntityException(loc(position() - 1, 1), "Expected array start");
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
