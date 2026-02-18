package uq.pac.rsvp.policy.ast.schema.attribute;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.policy.ast.schema.SchemaFileEntry;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public abstract class AttributeType implements SchemaFileEntry {

    private final boolean required;
    private final Map<String, String> annotations;

    private String definitionName;

    protected AttributeType(boolean required) {
        this.required = required;
        this.annotations = Collections.emptyMap();
    }

    protected AttributeType(boolean required, Map<String, String> annotations) {
        this.required = required;
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
    }

    public boolean isRequired() {
        return required;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
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

    @Override
    public abstract void accept(SchemaVisitor visitor);

    @Override
    public abstract <T> T compute(SchemaComputationVisitor<T> visitor);

    public static class AttributeTypeDeserialiser implements JsonDeserializer<AttributeType> {

        @Override
        public AttributeType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String type = json.getAsJsonObject().get("type").getAsString();

            // FIXME: Cedar treats attributes as required by default, but to Gson there is
            // no difference between a missing boolean and an explicitly false boolean.
            // Probably need to add some special handling for this.

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
