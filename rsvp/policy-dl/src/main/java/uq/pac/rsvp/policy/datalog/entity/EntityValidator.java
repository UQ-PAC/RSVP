package uq.pac.rsvp.policy.datalog.entity;

import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.policy.ast.schema.*;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.UndefinedEntityUIDName;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

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
        schema.entityTypes().forEach(e -> {
            for (String name : e.getEntityNamesEnum()) {
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
                throw new EntityException(ref.getLocation(), "Undefined entity reference: " + ref);
            }
        }

        return new EntitySet(updated);
    }

    private void validate(Entity entity) {
        EntityReference euid = entity.getEuid();
        if (uids.contains(euid)) {
            throw new EntityException(euid.getLocation(), "Duplicate entity: " + entity.getEuid());
        }
        uids.add(euid);

        // Prevent entity names (IDs) from having escaped characters
        // Partly because we use \t for processing Datalog, partly because
        // this can skew source locations and partly because there is no good reason
        // to have these characters in entity names in general
        if (ESCAPED.matcher(euid.getId()).find()) {
            throw new EntityException(euid.getLocation(), "Unsupported entity id (escaped characters): " + euid.getId());
        }

        // Prevent entity names from having '???' internal names
        if (euid.getId().equals(UndefinedEntityUIDName)) {
            throw new EntityException(euid.getLocation(), "Internal entity id: " + euid.getId());
        }

        EntityTypeDefinition def = schema.getEntityType(euid.getType());
        if (def == null) {
            Set<String> actionTypes =
                    schema.actions().stream().map(ActionDefinition::getType).collect(Collectors.toSet());
            if (actionTypes.contains(euid.getType())) {
                throw new EntityException(entity.getLocation(), "Action entity: " + entity.getEuid());
            } else {
                throw new EntityException(entity.getLocation(), "Undefined entity type: " + euid.getType());
            }
        }
        def.getShape().process(this, entity.getAttrs());

        for (EntityReference value : entity.getParents()) {
            if (value instanceof EntityReference parent) {
                // FIXME: Cache
                Set<String> memberOf = def.getMemberOfTypes().stream()
                        .map(EntityTypeDefinition::getName)
                        .collect(Collectors.toSet());
                memberOf.add(def.getName());
                if (!memberOf.contains(parent.getType())) {
                    throw new EntityException(value.getLocation(), "Unexpected parent type: " + parent.getType() + " expected one of " + memberOf);
                }
            }
        }
    }

    // FIXME: incorporate source locations
    private static <T extends EntityValue> T expectedType(EntityValue payload, Class<T> cls, String kind) {
        require(payload != null);
        if (!cls.isInstance(payload)) {
            throw new EntityException(payload.getLocation(), "Expected " + kind);
        }
        return cls.cast(payload);
    }

    @Override
    public void visitRecordTypeDefinition(RecordTypeDefinition rec, EntityValue payload) {
        RecordValue value = expectedType(payload, RecordValue.class, "record");
        rec.getAttributes().forEach((attr, type) -> {
            EntityValue attrValue = value.getValue(attr);
            if (attrValue != null) {
                type.process(this, attrValue);
            }
            if (attrValue == null && type.isRequired()) {
                throw new EntityException(payload.getLocation(), "Missing attribute: " + attr);
            }
        });

        value.forEach((attr, val) -> {
            if (!rec.hasAttribute(attr)) {
                // TODO: Reporting the location of the entire record here because attributes
                //       are currently encoded as string and do not have associated locations
                throw new EntityException(value.getLocation(), "Unexpected attribute: " + attr);
            }
        });
    }

    @Override
    public void visitSetTypeDefinition(SetTypeDefinition type, EntityValue payload) {
        SetValue value = expectedType(payload, SetValue.class, "set");
        value.getValues().forEach(v -> type.getElementType().process(this, v));
    }

    @Override
    public void visitEntityTypeReference(EntityTypeReference type, EntityValue payload) {
        EntityReference ref = expectedType(payload, EntityReference.class, "entity reference");
        EntityTypeDefinition definition = type.getDefinition();

        if (!definition.getName().equals(ref.getType())) {
            throw new EntityException(payload.getLocation(), "Unexpected type: expected" + definition.getName() + ", got " + ref.getType());
        }

        // FIXME: Need to check if enum supports no values
        if (!definition.getEntityNamesEnum().isEmpty()) {
            if (!definition.getEntityNamesEnum().contains(ref.getId())) {
                throw new EntityException(payload.getLocation(), "Unexpected ID " + ref.getId() +
                        " for type " + definition.getName() + ", expected one of " + definition.getEntityNamesEnum());
            }
        }

        // Track all references
        references.add(ref);
    }

    @Override
    public void visitBoolean(BooleanType type, EntityValue payload) {
        expectedType(payload, BooleanValue.class, "boolean");
    }

    @Override
    public void visitCommonTypeReference(CommonTypeReference type, EntityValue payload) {
        type.getDefinition().process(this, payload);
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
    public void visitDateTime(DateTimeType type, EntityValue payload) {
        throw new EntityException(payload.getLocation(), "Unsupported element: " + payload);
    }

    @Override
    public void visitDecimal(DecimalType type, EntityValue payload) {
        throw new EntityException(payload.getLocation(), "Unsupported element: " + payload);
    }

    @Override
    public void visitDuration(DurationType type, EntityValue payload) {
        throw new EntityException(payload.getLocation(), "Unsupported element: " + payload);
    }

    @Override
    public void visitIpAddress(IpAddressType type, EntityValue payload) {
        throw new EntityException(payload.getLocation(), "Unsupported element: " + payload);
    }

    @Override
    public void visitUnresolvedTypeReference(UnresolvedTypeReference type, EntityValue payload) {
        throw new TranslationError("Unresolved reference in schema: " + type);
    }

    @Override
    public void visitSchema(Schema schema, EntityValue payload) {
        throw new TranslationError("Schema in type visitor");
    }

    @Override
    public void visitEntityTypeDefinition(EntityTypeDefinition type, EntityValue payload) {
        throw new TranslationError("Entity definition in type in visitor");
    }

    @Override
    public void visitActionDefinition(ActionDefinition action, EntityValue payload) {
        throw new TranslationError("Action definition in type in visitor");
    }
}
