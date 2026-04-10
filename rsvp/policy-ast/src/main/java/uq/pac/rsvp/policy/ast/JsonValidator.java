package uq.pac.rsvp.policy.ast;

import com.google.gson.*;

import java.util.HashSet;
import java.util.Set;

public class JsonValidator {

    /**
     * Simple schema-based validator of json. The function returns true as long as
     * {@code jsonElement} follows the structure imposed by the {@code jsonSchema}
     * <p>
     * The schema specification is free-form json, here the function ensures that
     * The structure of both elements is the same (including object key names)
     * and types of primitives associated to keys match.
     * <p>
     * The schema specification allows providing optional key values by adding '?'
     * at the end of key names. Schema arrays are expected to be one-element arrays,
     * with the element providing the type of the element expected
     */
    public static boolean validate(JsonElement jsonSchema, JsonElement jsonElement) {
        if (jsonSchema == null) {
            throw new RuntimeException("Invalid Schema: " + jsonSchema.toString());
        }

        if (jsonElement == null) {
            return false;
        }

        if (!jsonSchema.getClass().equals(jsonElement.getClass())) {
            return false;
        }

        switch (jsonSchema) {
            case JsonObject schema -> {
                JsonObject element = (JsonObject) jsonElement;
                Set<String> schemaKeyset = new HashSet<>();

                for (String key : schema.keySet()) {
                    boolean required = true;
                    String elementKey = key;
                    if (key.endsWith("?")) {
                        required = false;
                        elementKey = key.substring(0, key.length() - 1);
                    }

                    if (required || element.has(elementKey)) {
                        if (!validate(schema.get(key), element.get(elementKey))) {
                            return false;
                        }
                    }
                    schemaKeyset.add(elementKey);
                }

                for (String key : element.keySet()) {
                    if (!schemaKeyset.contains(key)) {
                        return false;
                    }
                }
                return true;
            }
            case JsonArray schema -> {
                JsonArray element = (JsonArray) jsonElement;
                if (schema.size() != 1) {
                    throw new RuntimeException("Invalid Schema: " + schema);
                }
                JsonElement schemaElement = schema.get(0);
                for (JsonElement el : element.asList()) {
                    if (!validate(schemaElement, el)) {
                        return false;
                    }
                }
                return true;
            }
            case JsonPrimitive schema -> {
                JsonPrimitive element = (JsonPrimitive) jsonElement;

                return (schema.isBoolean() && element.isBoolean()) ||
                        (schema.isNumber() && element.isNumber()) ||
                        (schema.isString() && element.isString());
            }
            case JsonNull n -> {
                return true;
            }
            default -> throw new RuntimeException("Invalid schema: " + jsonSchema);
        }
    }
}
