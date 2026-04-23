package uq.pac.rsvp.policy.ast.deserilisation;

import com.google.gson.*;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;

import java.lang.reflect.Type;
import java.util.Map;

public class SchemaDeserialiser implements JsonDeserializer<Schema> {

    @Override
    public Schema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Schema result = new Schema();

        // TODO: error reporting
        if (json.isJsonObject()) {

            for (Map.Entry<String, JsonElement> definition : json.getAsJsonObject().entrySet()) {

                String name = definition.getKey();
                JsonElement value = definition.getValue();

                if (value.isJsonObject()) {

                    JsonObject namespace = value.getAsJsonObject();

                    namespace.addProperty("name", name);

                    JsonElement entityTypes = namespace.get("entityTypes");

                    if (entityTypes != null && entityTypes.isJsonObject()) {
                        for (Map.Entry<String, JsonElement> entityType : entityTypes.getAsJsonObject().entrySet()) {
                            if (entityType.getValue().isJsonObject()) {
                                JsonObject type = entityType.getValue().getAsJsonObject();
                                String typeName = entityType.getKey();

                                type.addProperty("name", name.isEmpty() ? typeName : name + "::" + typeName);
                            }
                        }
                    }

                    JsonElement actions = namespace.get("actions");

                    if (actions != null && actions.isJsonObject()) {
                        for (Map.Entry<String, JsonElement> action : actions.getAsJsonObject().entrySet()) {
                            if (action.getValue().isJsonObject()) {
                                JsonObject type = action.getValue().getAsJsonObject();
                                String id = action.getKey();

                                type.addProperty("type", name.isEmpty() ? "Action" : name + "::Action");
                                type.addProperty("eid", id);
                            }
                        }
                    }

                    JsonElement commonTypes = namespace.get("commonTypes");

                    if (commonTypes != null && commonTypes.isJsonObject()) {
                        for (Map.Entry<String, JsonElement> commonType : commonTypes.getAsJsonObject().entrySet()) {
                            if (commonType.getValue().isJsonObject()) {
                                JsonObject type = commonType.getValue().getAsJsonObject();
                                String typeName = commonType.getKey();

                                type.addProperty("definitionName",
                                        name.isEmpty() ? typeName : name + "::" + typeName);
                            }
                        }
                    }

                    result.add(context.deserialize(value, Namespace.class));
                }
            }
        }

        return result;
    }
}
