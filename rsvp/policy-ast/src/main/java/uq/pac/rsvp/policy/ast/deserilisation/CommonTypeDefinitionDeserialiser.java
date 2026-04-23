package uq.pac.rsvp.policy.ast.deserilisation;

import com.google.gson.*;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.*;

import java.lang.reflect.Type;
import java.util.Map;

public class CommonTypeDefinitionDeserialiser implements JsonDeserializer<CommonTypeDefinition> {

    @Override
    public CommonTypeDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (!json.isJsonObject()) {
            return new UnresolvedTypeReference();
        }

        JsonObject definition = json.getAsJsonObject();

        String type = definition.get("type").getAsString();
        JsonElement nameElem = definition.get("name");
        String name = nameElem != null ? nameElem.getAsString() : "unknown";

        Type attributeType = switch (type) {
            case "Record" -> {
                // Record attributes are required by Cedar by default. If there is no
                // explicit configuration, set required to true.
                JsonElement attributes = definition.get("attributes");

                if (attributes.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> attr : attributes.getAsJsonObject().entrySet()) {
                        if (attr.getValue().isJsonObject()) {
                            JsonObject value = attr.getValue().getAsJsonObject();
                            if (!value.has("required")) {
                                value.addProperty("required", true);
                            }
                        }
                    }
                }

                yield RecordTypeDefinition.class;
            }
            case "Set" -> SetTypeDefinition.class;
            default -> switch (name) {
                case "__cedar::String" -> StringType.class;
                case "__cedar::Long" -> LongType.class;
                case "__cedar::Bool" -> BooleanType.class;
                case "__cedar::datetime" -> DateTimeType.class;
                case "__cedar::decimal" -> DecimalType.class;
                case "__cedar::duration" -> DurationType.class;
                case "__cedar::ipaddr" -> IpAddressType.class;
                default -> UnresolvedTypeReference.class;
            };
        };

        return context.deserialize(json, attributeType);
    }

}
