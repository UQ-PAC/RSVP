package uq.pac.rsvp.policy.ast.schema.attribute;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class PrimitiveType extends AttributeType {
    public static enum Type {
        String, Long, Boolean
    }

    private final Type type;

    public PrimitiveType(Type type, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.type = type;
    }

    public PrimitiveType(Type type, Map<String, String> annotations) {
        super(false, annotations);
        this.type = type;
    }

    public PrimitiveType(Type type, boolean required) {
        super(required);
        this.type = type;
    }

    public PrimitiveType(Type type) {
        this(type, false);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitPrimitiveAttributeType(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitPrimitiveAttributeType(this);
    }

    public static class PrimitiveTypeDeserialiser implements JsonDeserializer<PrimitiveType> {

        @Override
        public PrimitiveType deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {

            String type = json.getAsJsonObject().get("type").getAsString();

            JsonObject attributeObject = json.getAsJsonObject();
            JsonElement required = attributeObject.get("required");
            JsonElement annotations = attributeObject.get("annotations");

            Type primativeType = switch (type) {
                case "String" -> Type.String;
                case "Long" -> Type.Long;
                case "Boolean" -> Type.Boolean;
                default -> throw new JsonParseException("Unknown expression type: " + type);
            };

            return new PrimitiveType(primativeType, required != null ? required.getAsBoolean() : false,
                    context.deserialize(annotations, HashMap.class));

        }
    }
}
