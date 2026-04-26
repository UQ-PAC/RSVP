package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityTypeDefinition extends SchemaStatement {

    private final String name;

    @SerializedName("memberOfTypes")
    private final Set<String> unresolvedMemberOfTypes;

    private final RecordTypeDefinition shape;

    @SerializedName("enum")
    private final Set<String> entityNamesEnum;

    private final Map<String, String> annotations;

    // Set during type resolution
    private Set<EntityTypeDefinition> resolvedMemberOfDefinitions;

    public EntityTypeDefinition(String name, Set<String> memberOfTypes, Map<String, CommonTypeDefinition> shape,
            Set<String> entityNamesEnum,
            Map<String, String> annotations) {
        this.name = name;
        this.unresolvedMemberOfTypes = memberOfTypes != null ? Set.copyOf(memberOfTypes) : Collections.emptySet();
        this.shape = new RecordTypeDefinition(shape);
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
        this.entityNamesEnum = entityNamesEnum != null ? Set.copyOf(entityNamesEnum) : Collections.emptySet();
    }

    public RecordTypeDefinition getShape() {
        return shape;
    }

    public EntityTypeDefinition(String name, Set<String> memberOfTypes, Map<String, CommonTypeDefinition> shape,
            Set<String> entityNamesEnum) {
        this(name, memberOfTypes, shape, entityNamesEnum, null);
    }

    public EntityTypeDefinition(String name, Set<String> memberOfTypes, Map<String, CommonTypeDefinition> shape) {
        this(name, memberOfTypes, shape, null, null);
    }

    public EntityTypeDefinition(String name) {
        this(name, null, null, null, null);
    }

    public EntityTypeDefinition() {
        this(null, null, null, null, null);
    }

    public void resolveMemberOfTypes(Schema schema, Namespace local) {
        resolvedMemberOfDefinitions = new HashSet<>();

        for (String entityType : unresolvedMemberOfTypes) {
            EntityTypeDefinition resolved = Schema.resolveEntityType(entityType, schema, local);

            if (resolved == null) {
                throw new SchemaResolutionException("Could not resolve memberOf entity type: " + entityType);
            }

            resolvedMemberOfDefinitions.add(resolved);
        }
    }

    /**
     * Return the fully qualified name of this type definition in the format
     * {@code Namespace::TypeName}
     * 
     * @return The fully qualified name of this type if this type is defined within
     *         a resolved namespace, {@code null} otherwise.
     */
    public String getName() {
        return name;
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

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitEntityTypeDefinition(this, payload);
    }
}
