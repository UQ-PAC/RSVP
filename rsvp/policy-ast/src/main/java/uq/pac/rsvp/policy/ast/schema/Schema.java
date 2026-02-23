package uq.pac.rsvp.policy.ast.schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import com.cedarpolicy.model.exception.InternalException;
import com.cedarpolicy.model.schema.Schema.JsonOrCedar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.common.DecimalType;
import uq.pac.rsvp.policy.ast.schema.common.DurationType;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class Schema extends HashMap<String, Namespace> {

    /**
     * Parse a schema in the Cedar format and return the corresponding AST.
     * 
     * @param schemaFile the path to the Cedar schema file
     * @return a Schema instance corresponding to the parsed Cedar schema file
     * @throws JsonProcessingException If the JSON produced by Cedar is invalid
     * @throws JsonMappingException    If the JSON produced by Cedar is invalid
     * @throws InternalException       If either Cedar parsing or the conversion
     *                                 from Cedar to JSON format fails
     * @throws IllegalStateException   If the Schema is empty? Unclear
     * @throws IOException             If an IO error occurs when reading the schema
     *                                 file
     */
    public static Schema parseCedarSchema(Path schemaFile) throws JsonMappingException, JsonProcessingException,
            InternalException, NullPointerException, IllegalStateException, IOException {
        String cedar = Files.readString(schemaFile);
        String json = com.cedarpolicy.model.schema.Schema.parse(JsonOrCedar.Cedar,
                cedar).toJsonFormat().toString();
        // Resolve types:
        // String json =
        // com.cedarpolicy.model.schema.Schema.parseCedarSchemaToResolvedJson(cedar);
        return parseJsonSchema(json);
    }

    /**
     * Parse a schema in the JSON format and return the corresponding AST.
     *
     * @param schemaFile the path to the Cedar schema file in the JSON format
     * @return a Schema instance corresponding to the parsed JSON schema file
     */
    public static Schema parseJsonSchema(Path schemaFile)
            throws NullPointerException, IllegalStateException, IOException {
        return parseJsonSchema(Files.readString(schemaFile));
    }

    /**
     * Parse a schema in the JSON format and return the corresponding AST.
     *
     * @param json schema in JSON format as string
     * @return a Schema instance corresponding to the parsed Cedar schema file
     */
    public static Schema parseJsonSchema(String json) throws NullPointerException, IllegalStateException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                .create();
        Schema result = gson.fromJson(json, Schema.class);
        SchemaVisitor visitor = new SchemaResolutionVisitor();
        visitor.visitSchema(result);
        return result;
    }

    // Cedar docs say type resolution order is:
    // common type > entity type > primitive/extension type
    public static CommonTypeDefinition resolveTypeReference(String name, Schema schema, Namespace local) {
        CommonTypeDefinition common = resolveCommonType(name, schema, local);

        if (common != null) {
            return new CommonTypeReference(common);
        }

        EntityTypeDefinition entity = resolveEntityType(name, schema, local);

        if (entity != null) {
            return new EntityTypeReference(entity);
        }

        return switch (name) {
            case "Boolean" -> new BooleanType();
            case "Long" -> new LongType();
            case "String" -> new StringType();
            case "datetime" -> new DateTimeType();
            case "decimal" -> new DecimalType();
            case "duration" -> new DurationType();
            case "ipaddr" -> new IpAddressType();
            default -> null;
        };

    }

    public static EntityTypeDefinition resolveEntityType(String entityType, Schema schema, Namespace local) {
        if (entityType.contains("::")) {
            String[] entityNameParts = entityType.split("::");
            if (schema.containsKey(entityNameParts[0])) {
                return schema.get(entityNameParts[0]).getEntityType(entityNameParts[1]);
            }
        } else {
            return local.getEntityType(entityType);
        }

        return null;
    }

    public static ActionDefinition resolveActionType(String id, String type, Schema schema, Namespace local) {

        if (type != null) {
            if (!type.endsWith("::Action")) {
                return null;
            }

            Namespace namespace = schema.get(type.substring(0, type.length() - 8));

            if (namespace == null) {
                return null;
            }

            return namespace.getAction(id);

        } else {
            return local.getAction(id);
        }

    }

    public static CommonTypeDefinition resolveCommonType(String attributeType, Schema schema, Namespace local) {
        if (attributeType.contains("::")) {
            String[] entityNameParts = attributeType.split("::");
            if (schema.containsKey(entityNameParts[0])) {
                return schema.get(entityNameParts[0]).getCommonType(entityNameParts[1]);
            }
        } else {
            return local.getCommonType(attributeType);
        }

        return null;
    }
}
