package uq.pac.rsvp.policy.datalog.entity;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.support.error.TranslationError;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;

/**
 * Validation of a set of entities with respect to a schema.
 * <p>
 * The main API function is {@link EntityValidator#validate(Schema, EntitySet)}
 * that ensures that the set of entities is consistent with the provided schema
 * and returns a possibly updated set of entities. For instance, if the input
 * set does not include entities from enum-style entity definitions this validator
 * will generate them
 */
public class EntityValidator implements SchemaPayloadVisitor<EntityValue> {

    private final Schema schema;
    private final EntitySet entities;
    // UIDs of all entities
    private Set<EntityReference> uids;
    // UIDs of all entity references
    private final Set<EntityReference> references;
    private final static Pattern ESCAPED = Pattern.compile("[\b\t\n\r\"\\\\]");

    private EntityValidator(Schema schema, EntitySet entities) {
        this.schema = schema;
        this.entities = entities;
        this.uids = new HashSet<>();
        this.references = new HashSet<>();
    }

    public static EntitySet validate(Schema schema, EntitySet entities) {
        return new EntityValidator(schema, entities).validate();
    }

    public synchronized EntitySet validate() {
        this.uids = new HashSet<>();
        entities.stream().forEach(this::validate);

        Set<Entity> updated = new HashSet<>(entities.getEntities());

        // At this point euids contain all references
        // Add references from enums
        schema.enumEntityTypes().forEach(e -> {
            for (String name : e.getEnumNames()) {
                EntityReference ref = new EntityReference(e.getName(), name);
                if (!uids.contains(ref)) {
                    uids.add(ref);
                    updated.add(new Entity(ref, new RecordValue(), new HashSet<>(), null));
                }
            }
        });

        // Check all references for existence
        for (EntityReference ref : references) {
            if (!uids.contains(ref)) {
                throw new TranslationError("Undefined entity reference: " + ref, ref.getSourceLoc());
            }
        }

        return new EntitySet(updated);
    }

    private void validate(Entity entity) {
        EntityReference euid = entity.getEuid();
        if (uids.contains(euid)) {
            throw new TranslationError("Duplicate entity: " + entity.getEuid(), euid.getSourceLoc());
        }
        uids.add(euid);

        // Prevent entity names (IDs) from having escaped characters
        // Partly because we use \t for processing Datalog, partly because
        // this can skew source locations and partly because there is no good reason
        // to have these characters in entity names in general
        if (ESCAPED.matcher(euid.getId()).find()) {
            throw new TranslationError("Unsupported entity id (escaped characters): " + euid.getId(), euid.getSourceLoc());
        }

        // Here we parse only the type. Then, if this is an action reference then the type name is 'Action'
        TypeReference ref = TypeReference.parse(euid.getType());
        EntityTypeDefinition def = schema.getEntityType(ref);
        if (def == null) {
            if (ref.getBaseName().equals("Action")) {
                throw new TranslationError( "Action entity: " + entity.getEuid(), entity.getSourceLoc());
            } else {
                throw new TranslationError( "Undefined entity type: " + euid.getType(), entity.getSourceLoc());
            }
        }
        def.getShape().process(this, entity.getAttrs());

        for (EntityReference value : entity.getParents()) {
            if (value instanceof EntityReference parent) {
                // FIXME: Cache
                Set<String> memberOf = def.getMemberOf().stream()
                        .map(TypeReference::getName)
                        .collect(Collectors.toSet());
                memberOf.add(def.getName());
                if (!memberOf.contains(parent.getType())) {
                    throw new TranslationError( "Unexpected parent type: " + parent.getType() + " expected one of " + memberOf, value.getSourceLoc());
                }
            }
        }
    }

    private static <T extends EntityValue> T expectedType(EntityValue payload, Class<T> cls, String kind) {
        require(payload != null);
        if (!cls.isInstance(payload)) {
            throw new TranslationError( "Expected " + kind, payload.getSourceLoc());
        }
        return cls.cast(payload);
    }

    @Override
    public void visitRecord(RecordType rec, EntityValue payload) {
        RecordValue value = expectedType(payload, RecordValue.class, "record");
        rec.getAttributes().forEach((attr, type) -> {
            EntityValue attrValue = value.getValue(new AttributeName(attr.getName()));
            if (attrValue != null) {
                type.process(this, attrValue);
            }
            if (attrValue == null && attr.isRequired()) {
                throw new TranslationError("Missing attribute: " + attr, payload.getSourceLoc());
            }
        });

        value.forEach((attr, val) -> {
            if (!rec.hasAttribute(attr.getValue())) {
                throw new TranslationError("Unexpected attribute: " + attr, attr.getSourceLoc());
            }
        });
    }

    @Override
    public void visitSet(SetType type, EntityValue payload) {
        SetValue value = expectedType(payload, SetValue.class, "set");
        value.getValues().forEach(v -> type.getElementType().process(this, v));
    }

    @Override
    public void visitRecordEntity(RecordEntityTypeDefinition type, EntityValue payload) {
        EntityReference ref = expectedType(payload, EntityReference.class, "entity reference");

        if (!type.getName().equals(ref.getType())) {
            throw new TranslationError("Unexpected type: expected" + type.getName() + ", got " + ref.getType(),
                    payload.getSourceLoc());
        }

        // Track all references
        references.add(ref);
    }

    @Override
    public void visitEnumEntity(EnumEntityTypeDefinition type, EntityValue payload) {
        EntityReference ref = expectedType(payload, EntityReference.class, "entity reference");

        if (!type.getName().equals(ref.getType())) {
            throw new TranslationError("Unexpected type: expected" + type.getName() + ", got " + ref.getType(), payload.getSourceLoc());
        }

        if (!type.getEnumNames().contains(ref.getId())) {
            throw new TranslationError("Unexpected ID " + ref.getId() +
                    " for type " + type.getName() + ", expected one of " + type.getEnumNames(), payload.getSourceLoc());
        }

        // Track all references
        references.add(ref);
    }

    @Override
    public void visitBoolean(BooleanType type, EntityValue payload) {
        expectedType(payload, BooleanValue.class, "boolean");
    }

    @Override
    public void visitTypeReference(TypeReference reference, EntityValue payload) {
        schema.getStatement(reference).process(this, payload);
    }

    @Override
    public void visitLong(LongType type, EntityValue payload) {
        expectedType(payload, LongValue.class, "long");
    }

    @Override
    public void visitString(StringType type, EntityValue payload) {
        expectedType(payload, StringValue.class, "string");
    }

    @Override
    public void visitCommon(CommonTypeDefinition type, EntityValue payload) {
        type.getDefinition().process(this, payload);
    }
}
