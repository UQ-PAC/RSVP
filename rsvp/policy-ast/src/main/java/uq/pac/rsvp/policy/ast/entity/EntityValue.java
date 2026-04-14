package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.*;
import uq.pac.rsvp.policy.ast.JsonParser;
import uq.pac.rsvp.policy.ast.JsonValidator;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class EntityValue {

    // FIXME: We need source locations here

    private final static JsonElement REFERENCE_SCHEMA = JsonParser.getGson().fromJson("""
            { "id" : "id", "type" : "type" }
            """, JsonElement.class);

    public static EntityValue deserialise(JsonElement json) {
        return switch (json) {
            case JsonObject object -> {
                if (object.has("__entity")) {
                    object = object.get("__entity").getAsJsonObject();
                }
                if (JsonValidator.validate(REFERENCE_SCHEMA, object)) {
                    yield new EntityReference(object.get("type").getAsString(), object.get("id").getAsString());
                } else {
                    yield new RecordValue(object.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey, e -> deserialise(e.getValue()))));
                }
            }
            case JsonArray arr ->
                new SetValue(arr.asList().stream().map(EntityValue::deserialise).collect(Collectors.toSet()));
            case JsonPrimitive prim -> {
                if (prim.isString()) {
                    yield new StringValue(prim.getAsString());
                } else if (prim.isNumber()) {
                    yield new LongValue(prim.getAsLong());
                } else if (prim.isBoolean()) {
                    yield new BooleanValue(prim.getAsBoolean());
                }
                throw new RuntimeException("Unsupported value: " + json);
            }
            default -> throw new RuntimeException("Unsupported value: " + json);
        };
    }
}
