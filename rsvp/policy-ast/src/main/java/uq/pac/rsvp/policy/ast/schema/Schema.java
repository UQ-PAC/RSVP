package uq.pac.rsvp.policy.ast.schema;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.cedarpolicy.model.exception.InternalException;
import com.cedarpolicy.model.schema.Schema.JsonOrCedar;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.JsonParser;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.common.DecimalType;
import uq.pac.rsvp.policy.ast.schema.common.DurationType;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.ast.schema.common.UnresolvedTypeReference;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class Schema extends HashMap<String, Namespace> implements SchemaItem {

    private final Map<String, EntityTypeDefinition> entityTypes;
    private final Map<String, Map<String, ActionDefinition>> actions;
    private final Map<String, CommonTypeDefinition> commonTypes;

    public Schema(Map<String, Namespace> other) {
        super(other);
        entityTypes = new HashMap<>();
        actions = new HashMap<>();
        commonTypes = new HashMap<>();
    }

    public Schema() {
        super();
        entityTypes = new HashMap<>();
        actions = new HashMap<>();
        commonTypes = new HashMap<>();
    }

    public void add(Namespace namespace) {
        this.put(namespace.getName(), namespace);
    }

    public Set<String> entityTypeNames() {
        return Set.copyOf(entityTypes.keySet());
    }

    public Collection<EntityTypeDefinition> entityTypes() {
        return Set.copyOf(entityTypes.values());
    }

    public EntityTypeDefinition getEntityType(String name) {
        return entityTypes.get(name);
    }

    public void addEntityType(EntityTypeDefinition type) {
        entityTypes.put(type.getName(), type);
    }

    public Set<String> actionTypes() {
        return Set.copyOf(actions.keySet());
    }

    public Set<String> actionNames(String type) {
        Map<String, ActionDefinition> typedActions = actions.get(type);

        if (typedActions == null) {
            return Collections.emptySet();
        }

        return Set.copyOf(typedActions.keySet());
    }

    public ActionDefinition getAction(String type, String id) {
        Map<String, ActionDefinition> typedActions = actions.get(type);

        if (typedActions != null) {
            return typedActions.get(id);
        }

        return null;
    }

    public Collection<ActionDefinition> actions() {
        List<ActionDefinition> definitions = new ArrayList<>();
        actions.forEach((a, m) -> {
            definitions.addAll(m.values());
        });
        return definitions;
    }

    public void addAction(ActionDefinition action) {

        Map<String, ActionDefinition> typedActions = actions.get(action.getType());

        if (typedActions == null) {
            typedActions = new HashMap<>();
            actions.put(action.getType(), typedActions);
        }

        typedActions.put(action.getName(), action);
    }

    public Set<String> commonTypeNames() {
        return Set.copyOf(commonTypes.keySet());
    }

    public CommonTypeDefinition getCommonType(String name) {
        return commonTypes.get(name);
    }

    public void addCommonType(CommonTypeDefinition type) {
        commonTypes.put(type.getName(), type);
    }

    /**
     * Parse a schema in the Cedar format and return the corresponding AST.
     * 
     * @param schemaFile the path to the Cedar schema file
     * @return a Schema instance corresponding to the parsed Cedar schema file
     */
    public static Schema parseCedarSchema(Path schemaFile) throws RsvpException {
        String cedar;
        try {
            cedar = Files.readString(schemaFile);

            String json = com.cedarpolicy.model.schema.Schema.parse(JsonOrCedar.Cedar,
                    cedar).toJsonFormat().toString();
            return parseJsonSchema(json);
        } catch (IOException | InternalException | NullPointerException | IllegalStateException e) {
            throw new RsvpException("Error parsing schema in " + schemaFile, e);
        }
    }

    /**
     * Parse a schema in the JSON format and return the corresponding AST.
     *
     * @param schemaFile the path to the Cedar schema file in the JSON format
     * @return a Schema instance corresponding to the parsed JSON schema file
     */
    public static Schema parseJsonSchema(Path schemaFile) throws RsvpException {
        try {
            return parseJsonSchema(Files.readString(schemaFile));
        } catch (IOException | NullPointerException | IllegalStateException e) {
            throw new RsvpException("Error parsing schema in " + schemaFile, e);
        }
    }

    /**
     * Parse a schema in the JSON format and return the corresponding AST.
     *
     * @param json schema in JSON format as string
     * @return a Schema instance corresponding to the parsed Cedar schema file
     */
    public static Schema parseJsonSchema(String json) throws RsvpException {
        try {
            Schema result = JsonParser.parseSchema(json);
            SchemaVisitor visitor = new SchemaResolutionVisitor();
            visitor.visitSchema(result);
            return result;
        } catch (NullPointerException | IllegalStateException e) {
            throw new RsvpException("Error parsing JSON schema", e);
        }
    }

    /**
     * Resolve a type reference with respect to the namespace where the reference
     * was defined, based on Cedar type resolution rules.
     * In the case of shadowed type names, resolution will occur in the following
     * order:
     * <ol>
     * <li>Common type</li>
     * <li>Entity type</li>
     * <li>Primitive or extension type</li>
     * </ol>
     * 
     * To specify that a type should definitely be a primitive or extension type,
     * the prefix {@code __cedar::} can be prepended to the type name, in which case
     * this resolution will not be executed.
     * 
     * @param unresolved the {@code UnresolvedTypeReference} node to resolve based
     *                   on name
     * @param schema     the schema to query
     * @param local      the local namespace where the unresolved reference was
     *                   defined
     * @return A resolved type node
     * @throws SchemaResolutionException if the type could not be resolved
     */
    public static CommonTypeDefinition resolveTypeReference(UnresolvedTypeReference unresolved, Schema schema,
            Namespace local) {
        // Cedar docs say type resolution order is:
        // common type > entity type > primitive/extension type
        if (unresolved == null) {
            return null;
        }

        String referenceTypeName = unresolved.getRawTypeName();

        if (referenceTypeName == null) {
            throw new SchemaResolutionException("Reference type name was undefined");
        }

        String definitionName = unresolved.getName();

        CommonTypeDefinition common = resolveCommonType(referenceTypeName, schema, local);

        if (common != null) {
            return new CommonTypeReference(definitionName, common, unresolved.isRequired());
        }

        EntityTypeDefinition entity = resolveEntityType(referenceTypeName, schema, local);

        if (entity != null) {
            return new EntityTypeReference(definitionName, entity, unresolved.isRequired());
        }

        return switch (referenceTypeName) {
            case "Bool" -> new BooleanType(definitionName, unresolved.isRequired());
            case "Long" -> new LongType(definitionName, unresolved.isRequired());
            case "String" -> new StringType(definitionName, unresolved.isRequired());
            case "datetime" -> new DateTimeType(definitionName, unresolved.isRequired());
            case "decimal" -> new DecimalType(definitionName, unresolved.isRequired());
            case "duration" -> new DurationType(definitionName, unresolved.isRequired());
            case "ipaddr" -> new IpAddressType(definitionName, unresolved.isRequired());
            default -> throw new SchemaResolutionException("Could not resolve type: " + referenceTypeName);
        };

    }

    /**
     * Get the definition corresponding to an entity type reference based on Cedar
     * type resolution rules.
     * 
     * @param entityType the name of the referenced entity to be resolved
     * @param schema     the schema to query
     * @param local      the namespace containing the reference
     * @return the entity definition referenced by the supplied name with respect to
     *         the supplied namespace, or {@code null} if none could be found
     */
    public static EntityTypeDefinition resolveEntityType(String entityType, Schema schema, Namespace local) {

        EntityTypeDefinition result = null;

        if (entityType != null) {
            if (entityType.contains("::")) {
                String[] entityNameParts = entityType.split("::");
                if (schema.containsKey(entityNameParts[0])) {
                    result = schema.get(entityNameParts[0]).getEntityType(entityNameParts[1]);
                }
            } else {
                result = local.getEntityType(entityType);

                if (result == null) {
                    // Try top-level namespace
                    result = schema.getEntityType(entityType);
                }
            }
        }

        return result;
    }

    /**
     * Get the definition corresponding to an action reference based on Cedar type
     * resolution rules.
     * Note that actions cannot be shadowed. If an action is defined in the global
     * namespace, it cannot
     * also be defined in any other namespace.
     * 
     * @param type   the type of the referenced action to be resolved (e.g.
     *               {@code Namespace::Action})
     * @param id     the name of the referenced action to be resolved
     * @param schema the schema to query
     * @param local  the namespace containing the reference
     * @return the action definition referenced by the supplied name with respect to
     *         the supplied namespace, or {@code null} if none could be found
     * @throws SchemaResolutionException if the action type is malformed or the
     *                                   type's namespace cannot be resolved
     */
    public static ActionDefinition resolveActionType(String type, String id, Schema schema, Namespace local) {

        if (id == null) {
            return null;
        }

        ActionDefinition result = null;

        if (type == null || type.equals("Action")) {
            result = local.getAction(id);

            if (result == null) {
                // Try top-level namespace
                result = schema.getAction("Action", id);
            }

        } else {
            if (!type.endsWith("::Action")) {
                throw new SchemaResolutionException("Malformed action type: " + type);
            }

            String localName = type.substring(0, Math.max(0, type.length() - 8));
            Namespace namespace = schema.get(localName);

            if (namespace == null) {
                throw new SchemaResolutionException("Unknown namespace: " + localName);
            }

            result = namespace.getAction(id);
        }

        return result;

    }

    /**
     * Get the definition corresponding to an common type reference based on Cedar
     * type resolution rules.
     * 
     * @param attributeType the name of the referenced type to be resolved
     * @param schema        the schema to query
     * @param local         the namespace containing the reference
     * @return the type definition referenced by the supplied name with respect to
     *         the supplied namespace, or {@code null} if none could be found
     */
    public static CommonTypeDefinition resolveCommonType(String attributeType, Schema schema, Namespace local) {
        CommonTypeDefinition result = null;

        if (attributeType != null) {
            if (attributeType.contains("::")) {
                String[] entityNameParts = attributeType.split("::");
                if (schema.containsKey(entityNameParts[0])) {
                    result = schema.get(entityNameParts[0]).getCommonType(entityNameParts[1]);
                }
            } else {
                result = local.getCommonType(attributeType);

                if (result == null) {
                    // Try top-level namespace
                    result = schema.getCommonType(attributeType);
                }
            }
        }

        return result;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitSchema(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitSchema(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitSchema(this, payload);
    }

    public static class SchemaDeserialiser implements JsonDeserializer<Schema> {

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

}
