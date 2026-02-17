package uq.pac.rsvp.policy.ast.schema.attribute;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public abstract class AttributeType {

    private String definitionName;
    private boolean required;
    private Map<String, String> annotations;

    protected AttributeType(boolean required) {
        this.required = required;
    }

    protected AttributeType(boolean required, Map<String, String> annnotations) {
        this(required);
        this.annotations = annotations != null ? new HashMap<>(annnotations) : Collections.emptyMap();
    }

    public boolean isRequired() {
        return required;
    }

    public Map<String, String> getAnnotations() {
        return new HashMap<>(annotations);
    }

    /**
     * If this type was defined in a record, entity shape or as a common type, then
     * return the name this attribute type was mapped to within that structure.
     */
    public final String getDefinitionName() {
        return definitionName;
    }

    public final void setDefinitionName(String name) {
        this.definitionName = name;
    }

    public abstract void accept(SchemaVisitor visitor);

    public static class AttributeTypeDeserialiser implements JsonDeserializer<AttributeType> {

        @Override
        public AttributeType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String type = json.getAsJsonObject().get("type").getAsString();

            Type attributeType = switch (type) {
                case "String" -> PrimitiveType.class;
                case "Long" -> PrimitiveType.class;
                case "Boolean" -> PrimitiveType.class;
                case "Record" -> RecordType.class;
                case "Set" -> SetType.class;
                case "Entity" -> EntityOrCommonType.class;
                case "Extension" -> ExtensionType.class;
                case "EntityOrCommon" -> EntityOrCommonType.class;
                default -> throw new JsonParseException("Unknown expression type: " + type);
            };

            return context.deserialize(json, attributeType);
        }

    }
}
