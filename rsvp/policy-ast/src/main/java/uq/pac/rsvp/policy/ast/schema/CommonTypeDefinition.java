package uq.pac.rsvp.policy.ast.schema;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.common.DecimalType;
import uq.pac.rsvp.policy.ast.schema.common.DurationType;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.ast.schema.common.UnresolvedTypeReference;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public abstract class CommonTypeDefinition implements SchemaFileEntry {

    // If this type definition is a record property, this may be true. Otherwise,
    // it will be false
    private final boolean required;

    private final Map<String, String> annotations;

    // Set during type resolution to the key this type was mapped to in the
    // namespace, if any. Types defined in records and sets will not be named.
    private String definitionName;

    protected CommonTypeDefinition(boolean required, Map<String, String> annotations) {
        this.required = required;
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
    }

    protected CommonTypeDefinition(boolean required) {
        this(required, null);
    }

    protected CommonTypeDefinition(Map<String, String> annotations) {
        this(true, annotations);
    }

    protected CommonTypeDefinition() {
        this(true, null);
    }

    public boolean isRequired() {
        return required;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    /**
     * If this type was defined as a common type within a resolved namespace, then
     * return the fully qualified name of this type definition in the format
     * {@code Namespace::TypeName}
     * 
     * @return The fully qualified name of this type if this type is defined within
     *         a resolved namespace, {@code null} otherwise.
     */
    public final String getName() {
        return definitionName;
    }

    public final void setName(String name) {
        this.definitionName = name;
    }

    @Override
    public abstract void accept(SchemaVisitor visitor);

    @Override
    public abstract <T> T compute(SchemaComputationVisitor<T> visitor);

    public static class CommonTypeDefinitionDeserialiser implements JsonDeserializer<CommonTypeDefinition> {

        @Override
        public CommonTypeDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String type = json.getAsJsonObject().get("type").getAsString();
            JsonElement nameElem = json.getAsJsonObject().get("name");
            String name = nameElem != null ? nameElem.getAsString() : "unknown";

            // FIXME: Cedar treats attributes as required by default, but to Gson there is
            // no difference between a missing boolean and an explicitly false boolean.
            // Probably need to add some special handling for this.

            Type attributeType = switch (type) {
                case "Record" -> RecordTypeDefinition.class;
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
}
