package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityTypeDefinition implements SchemaItem {

    @SerializedName("memberOfTypes")
    private final Set<String> unresolvedMemberOfTypes;

    private final RecordTypeDefinition shape;

    @SerializedName("enum")
    private final Set<String> entityNamesEnum;

    private final Map<String, String> annotations;

    // Set during type resolution
    private Set<EntityTypeDefinition> resolvedMemberOfDefinitions;

    // Set during type resolution to the key this entity was mapped to in the
    // namespace
    private String name;

    public EntityTypeDefinition(Set<String> memberOfTypes, Map<String, CommonTypeDefinition> shape,
            Set<String> entityNamesEnum,
            Map<String, String> annotations) {
        this.unresolvedMemberOfTypes = memberOfTypes != null ? Set.copyOf(memberOfTypes) : Collections.emptySet();
        this.shape = new RecordTypeDefinition(shape);
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
        this.entityNamesEnum = entityNamesEnum != null ? Set.copyOf(entityNamesEnum) : Collections.emptySet();
    }

    public EntityTypeDefinition(Set<String> memberOfTypes, Map<String, CommonTypeDefinition> shape,
            Set<String> entityNamesEnum) {
        this(memberOfTypes, shape, entityNamesEnum, null);
    }

    public EntityTypeDefinition(Set<String> memberOfTypes, Map<String, CommonTypeDefinition> shape) {
        this(memberOfTypes, shape, null, null);
    }

    public EntityTypeDefinition() {
        this(null, null, null, null);
    }

    public void resolveMemberOfTypes(Schema schema, Namespace local) {
        resolvedMemberOfDefinitions = new HashSet<>();

        for (String entityType : unresolvedMemberOfTypes) {
            EntityTypeDefinition resolved = Schema.resolveEntityType(entityType, schema, local);

            if (resolved != null) {
                resolvedMemberOfDefinitions.add(resolved);
            }
        }
    }

    /**
     * If this type is defined within a resolved namespace, then return the fully
     * qualified name of this type definition in the format
     * {@code Namespace::TypeName}
     * 
     * @return The fully qualified name of this type if this type is defined within
     *         a resolved namespace, {@code null} otherwise.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<EntityTypeDefinition> getMemberOfTypes() {
        return resolvedMemberOfDefinitions != null ? Set.copyOf(resolvedMemberOfDefinitions) : Collections.emptySet();
    }

    public Set<String> getShapeAttributeNames() {
        return shape.getAttributeNames();
    }

    public CommonTypeDefinition getShapeAttributeType(String name) {
        return shape.getAttributeType(name);
    }

    public void resolveShapeAttributeType(String name, CommonTypeDefinition type) {
        shape.resolveAttributeType(name, type);
    }

    public Set<String> getEntityNamesEnum() {
        return entityNamesEnum;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitEntityTypeDefinition(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitEntityTypeDefinition(this);
    }
}
