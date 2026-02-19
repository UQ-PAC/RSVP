package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityType implements SchemaFileEntry {

    private final Set<String> memberOfTypes;
    private final RecordType shape;

    @SerializedName("enum")
    private final Set<String> entityNamesEnum;

    private final Map<String, String> annotations;

    // Resolved memberOfTypes
    private Set<EntityType> memberOfTypeRefs;

    // Any unresolved memberOfTypes
    private Set<String> unresolvedMemberOfTypes;

    // The name that this entity is mapped to in the namespace
    private String name;

    public EntityType(Set<String> memberOfTypes, Map<String, AttributeType> shape) {
        this.memberOfTypes = memberOfTypes != null ? Set.copyOf(memberOfTypes) : Collections.emptySet();
        this.shape = new RecordType(shape);
        this.annotations = Collections.emptyMap();
        this.entityNamesEnum = Collections.emptySet();
    }

    public EntityType(Set<String> memberOfTypes, Map<String, AttributeType> shape, Set<String> entityNamesEnum) {
        this.memberOfTypes = memberOfTypes != null ? Set.copyOf(memberOfTypes) : Collections.emptySet();
        this.shape = new RecordType(shape);
        this.entityNamesEnum = entityNamesEnum != null ? Set.copyOf(entityNamesEnum) : Collections.emptySet();
        this.annotations = Collections.emptyMap();
    }

    public EntityType(Set<String> memberOfTypes, Map<String, AttributeType> shape, Set<String> entityNamesEnum,
            Map<String, String> annotations) {
        this.memberOfTypes = memberOfTypes != null ? Set.copyOf(memberOfTypes) : Collections.emptySet();
        this.shape = new RecordType(shape);
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
        this.entityNamesEnum = entityNamesEnum != null ? Set.copyOf(entityNamesEnum) : Collections.emptySet();
    }

    public void resolveMemberOfTypes(Schema schema, Namespace local) {
        memberOfTypeRefs = new HashSet<>();

        if (memberOfTypes != null) {
            for (String entityType : memberOfTypes) {
                EntityType resolved = Schema.resolveEntityType(entityType, schema, local);

                if (resolved != null) {
                    memberOfTypeRefs.add(resolved);
                } else {
                    unresolvedMemberOfTypes.add(entityType);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<EntityType> getMemberOfTypes() {
        return memberOfTypeRefs != null ? Set.copyOf(memberOfTypeRefs) : Collections.emptySet();
    }

    public Set<String> getShapeAttributeNames() {
        return shape != null ? shape.getAttributeNames() : Collections.emptySet();
    }

    public AttributeType getShapeAttributeType(String name) {
        return shape != null ? shape.getAttributeType(name) : null;
    }

    public Set<String> getEntityNamesEnum() {
        return entityNamesEnum != null ? entityNamesEnum : Collections.emptySet();
    }

    public Map<String, String> getAnnotations() {
        return annotations != null ? annotations : Collections.emptyMap();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitEntityType(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitEntityType(this);
    }
}
