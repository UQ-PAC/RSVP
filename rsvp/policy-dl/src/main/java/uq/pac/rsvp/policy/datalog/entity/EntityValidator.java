package uq.pac.rsvp.policy.datalog.entity;

import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.policy.ast.schema.*;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.OUTPUT_DELIMITER;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.UndefinedEntityUIDName;

public class EntityValidator implements SchemaPayloadVisitor<EntityValue> {

    private final Schema schema;
    private final EntitySet entities;
    private Set<EntityReference> uids;

    private EntityValidator(Schema schema, EntitySet entities) {
        this.schema = schema;
        this.entities = entities;
        this.uids = new HashSet<>();
    }

    public static void validate(Schema schema, EntitySet entities) {
        new EntityValidator(schema, entities).validate();
    }

    static class Error extends RuntimeException {
        Error(String msg) {
            super(msg);
        }
    }

    public synchronized void validate() {
        this.uids = new HashSet<>();
        // FIXME: Report source locations on fail
        entities.stream().forEach(this::validate);
    }

    private void validate(Entity entity) {
        EntityReference euid = entity.getEuid();
        if (uids.contains(euid)) {
            throw new Error("Duplicate entity: " + entity.getEuid());
        }
        uids.add(euid);

        // Prevent entity names from having '\t' used as an internal delimiter
        if (euid.getId().contains(OUTPUT_DELIMITER)) {
            throw new Error("Unsupported entity name including tab characters: " + euid);
        }

        // Prevent entity names from having '???' internal names
        if (euid.getId().equals(UndefinedEntityUIDName)) {
            throw new TranslationError("Internal entity name in schema: " + euid);
        }

        EntityTypeDefinition def = schema.getEntityType(euid.getType());
        if (def == null) {
            Set<String> actionTypes =
                    schema.actions().stream().map(ActionDefinition::getType).collect(Collectors.toSet());
            if (actionTypes.contains(euid.getType())) {
                throw new Error("Action entity: " + entity.getEuid() + " in entity set");
            } else {
                throw new Error("Definition of " + euid.getType() + " not found in the schema");
            }
        }
        def.getShape().process(this, entity.getAttrs());

        for (EntityValue value : entity.getParents()) {
            if (value instanceof EntityReference parent) {
                // FIXME: Cache
                Set<String> memberOf = def.getMemberOfTypes().stream()
                        .map(EntityTypeDefinition::getName)
                        .collect(Collectors.toSet());
                memberOf.add(def.getName());
                if (!memberOf.contains(parent.getType())) {
                    throw new Error("Unexpected parent type: " + parent.getType() + " expected one of " + memberOf);
                }
            } else {
                throw new Error("Unexpected value: " + value + " expected entity reference");
            }
        }
    }

    // FIXME: incorporate source locations
    private static <T extends EntityValue> T expectedType(EntityValue payload, Class<T> cls, String kind) {
        if (payload == null) {
            throw new Error("Missing " + kind + " value");
        }
        if (!cls.isInstance(payload)) {
            throw new Error("Expected " + kind +  ", got value: " + payload);
        }
        return cls.cast(payload);
    }

    private void unsupported(SchemaItem item) {
        throw new Error("Unsupported schema component: " + item);
    }

    @Override
    public void visitRecordTypeDefinition(RecordTypeDefinition rec, EntityValue payload) {
        RecordValue value = expectedType(payload, RecordValue.class, "record");
        rec.getAttributes().forEach((attr, type) -> {
            EntityValue attrValue = value.getValue(attr);
            if (attrValue != null || type.isRequired()) {
                type.process(this, attrValue);
            }
        });

        value.forEach((attr, val) -> {
            if (!rec.hasAttribute(attr)) {
                throw new Error("Unexpected attribute: " + attr);
            }
        });
    }

    @Override
    public void visitSetTypeDefinition(SetTypeDefinition type, EntityValue payload) {
        SetValue value = expectedType(payload, SetValue.class, "set");
        value.getValues().forEach(v -> {
            type.getElementType().process(this, v);
        });
    }

    @Override
    public void visitEntityTypeReference(EntityTypeReference type, EntityValue payload) {
        EntityReference ref = expectedType(payload, EntityReference.class, "entity reference");
        EntityTypeDefinition definition = type.getDefinition();

        if (!definition.getName().equals(ref.getType())) {
            throw new Error("Unexpected type: expected" + definition.getName() + ", got " + ref.getType());
        }

        // FIXME: Need to check if enum supports no values
        if (!definition.getEntityNamesEnum().isEmpty()) {
            if (!definition.getEntityNamesEnum().contains(ref.getId())) {
                throw new Error("Unexpected ID " + ref.getId() +
                        " for type " + definition.getName() + ", expected one of " + definition.getEntityNamesEnum());
            }
        }
    }

    @Override
    public void visitBoolean(BooleanType type, EntityValue payload) {
        expectedType(payload, BooleanValue.class, "boolean");
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
        unsupported(schema);
    }

    @Override
    public void visitDecimal(DecimalType type, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitDuration(DurationType type, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitIpAddress(IpAddressType type, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitUnresolvedTypeReference(UnresolvedTypeReference type, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitSchema(Schema schema, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitEntityTypeDefinition(EntityTypeDefinition type, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitActionDefinition(ActionDefinition action, EntityValue payload) {
        unsupported(schema);
    }

    @Override
    public void visitCommonTypeReference(CommonTypeReference type, EntityValue payload) {
        type.getDefinition().process(this, payload);
    }
}
