package uq.pac.rsvp.policy.datalog.entity;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.entity.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.UndefinedEntityUIDName;
import static uq.pac.rsvp.Assertion.require;

/**
 * Validation of a set of entities with respect to a schema.
 * <p>
 * The main API function is {@link EntityValidator#validate(AntlrSchema, EntitySet)}
 * that ensures that the set of entities is consistent with the provided schema
 * and returns a possibly updated set of entities. For instance, if the input
 * set does not include entities from enum-style entity definitions this validator
 * will generate them
 */
public class EntityValidator implements AntlrSchemaPayloadVisitor<EntityValue> {

    private final AntlrSchema schema;
    private final EntitySet entities;
    // UIDs of all entities
    private Set<EntityReference> uids;
    // UIDs of all entity references
    private final Set<EntityReference> references;
    private final static Pattern ESCAPED = Pattern.compile("[\b\t\n\r\"\\\\]");

    private EntityValidator(AntlrSchema schema, EntitySet entities) {
        this.schema = schema;
        this.entities = entities;
        this.uids = new HashSet<>();
        this.references = new HashSet<>();
    }

    public static EntitySet validate(AntlrSchema schema, EntitySet entities) {
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
                throw new EntityException(ref.getSourceLoc(), "Undefined entity reference: " + ref);
            }
        }

        return new EntitySet(updated);
    }

    private void validate(Entity entity) {
        EntityReference euid = entity.getEuid();
        if (uids.contains(euid)) {
            throw new EntityException(euid.getSourceLoc(), "Duplicate entity: " + entity.getEuid());
        }
        uids.add(euid);

        // Prevent entity names (IDs) from having escaped characters
        // Partly because we use \t for processing Datalog, partly because
        // this can skew source locations and partly because there is no good reason
        // to have these characters in entity names in general
        if (ESCAPED.matcher(euid.getId()).find()) {
            throw new EntityException(euid.getSourceLoc(), "Unsupported entity id (escaped characters): " + euid.getId());
        }

        // Prevent entity names from having '???' internal names
        if (euid.getId().equals(UndefinedEntityUIDName)) {
            throw new EntityException(euid.getSourceLoc(), "Internal entity id: " + euid.getId());
        }

        // Here we parse only the type. Then, if this is an action reference then the type name is 'Action'
        AntlrTypeReference ref = AntlrTypeReference.parse(euid.getType());
        AntlrEntityType def = schema.getEntityType(ref);
        if (def == null) {
            if (ref.getBaseName().equals("Action")) {
                throw new EntityException(entity.getSourceLoc(), "Action entity: " + entity.getEuid());
            } else {
                throw new EntityException(entity.getSourceLoc(), "Undefined entity type: " + euid.getType());
            }
        }
        def.getShape().process(this, entity.getAttrs());

        for (EntityReference value : entity.getParents()) {
            if (value instanceof EntityReference parent) {
                // FIXME: Cache
                Set<String> memberOf = def.getMemberOf().stream()
                        .map(AntlrTypeReference::getName)
                        .collect(Collectors.toSet());
                memberOf.add(def.getName());
                if (!memberOf.contains(parent.getType())) {
                    throw new EntityException(value.getSourceLoc(), "Unexpected parent type: " + parent.getType() + " expected one of " + memberOf);
                }
            }
        }
    }

    // FIXME: incorporate source locations
    private static <T extends EntityValue> T expectedType(EntityValue payload, Class<T> cls, String kind) {
        require(payload != null);
        if (!cls.isInstance(payload)) {
            throw new EntityException(payload.getSourceLoc(), "Expected " + kind);
        }
        return cls.cast(payload);
    }

    @Override
    public void visitRecord(AntlrRecordType rec, EntityValue payload) {
        RecordValue value = expectedType(payload, RecordValue.class, "record");
        rec.getAttributes().forEach((attr, type) -> {
            EntityValue attrValue = value.getValue(new AttributeName(attr.getName()));
            if (attrValue != null) {
                type.process(this, attrValue);
            }
            if (attrValue == null && attr.isRequired()) {
                throw new EntityException(payload.getSourceLoc(), "Missing attribute: " + attr);
            }
        });

        value.forEach((attr, val) -> {
            if (!rec.hasAttribute(attr.getValue())) {
                throw new EntityException(attr.getSourceLoc(), "Unexpected attribute: " + attr);
            }
        });
    }

    @Override
    public void visitSet(AntlrSetType type, EntityValue payload) {
        SetValue value = expectedType(payload, SetValue.class, "set");
        value.getValues().forEach(v -> type.getElementType().process(this, v));
    }

    @Override
    public void visitRecordEntity(AntlrRecordEntityType type, EntityValue payload) {
        EntityReference ref = expectedType(payload, EntityReference.class, "entity reference");

        if (!type.getName().equals(ref.getType())) {
            throw new EntityException(payload.getSourceLoc(),
                    "Unexpected type: expected" + type.getName() + ", got " + ref.getType());
        }

        // Track all references
        references.add(ref);
    }

    @Override
    public void visitEnumEntity(AntlrEnumEntityType type, EntityValue payload) {
        EntityReference ref = expectedType(payload, EntityReference.class, "entity reference");

        if (!type.getName().equals(ref.getType())) {
            throw new EntityException(payload.getSourceLoc(), "Unexpected type: expected" + type.getName() + ", got " + ref.getType());
        }

        if (!type.getEnumNames().contains(ref.getId())) {
            throw new EntityException(payload.getSourceLoc(), "Unexpected ID " + ref.getId() +
                    " for type " + type.getName() + ", expected one of " + type.getEnumNames());
        }

        // Track all references
        references.add(ref);
    }

    @Override
    public void visitBoolean(AntlrBooleanType type, EntityValue payload) {
        expectedType(payload, BooleanValue.class, "boolean");
    }

    @Override
    public void visitTypeReference(AntlrTypeReference reference, EntityValue payload) {
        schema.getStatement(reference).process(this, payload);
    }

    @Override
    public void visitLong(AntlrLongType type, EntityValue payload) {
        expectedType(payload, LongValue.class, "long");
    }

    @Override
    public void visitString(AntlrStringType type, EntityValue payload) {
        expectedType(payload, StringValue.class, "string");
    }
}
