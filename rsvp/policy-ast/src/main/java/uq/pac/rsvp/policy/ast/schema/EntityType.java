package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class EntityType {

    private String name;

    private Set<String> memberOfTypes;
    private RecordType shape;

    @SerializedName("enum")
    private Set<String> entityNamesEnum;

    private Map<String, String> annotations;

    private Set<EntityType> memberOfTypeRefs;

    private Set<String> unresolvedMemberOfTypes;

    public EntityType(Set<String> memberOfTypes, Map<String, AttributeType> shape) {
        this.memberOfTypes = memberOfTypes != null ? new HashSet<>(memberOfTypes) : Collections.emptySet();
        this.shape = new RecordType(shape);
    }

    public EntityType(Set<String> memberOfTypes, Map<String, AttributeType> shape, Map<String, String> annotations) {
        this(memberOfTypes, shape);
        this.annotations = annotations != null ? new HashMap<>(annotations) : Collections.emptyMap();
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
        return memberOfTypeRefs != null ? new HashSet<>(memberOfTypeRefs) : Collections.emptySet();
    }

    public Set<String> getShapeAttributeNames() {
        return shape != null ? shape.getAttributeNames() : Collections.emptySet();
    }

    public AttributeType getShapeAttributeType(String name) {
        return shape != null ? shape.getAttributeType(name) : null;
    }

    public Set<String> getEntityNamesEnum() {
        return entityNamesEnum != null ? new HashSet<>(entityNamesEnum) : Collections.emptySet();
    }

    public Map<String, String> getAnnotations() {
        return annotations != null ? new HashMap<>(annotations) : Collections.emptyMap();
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visitEntityType(this);
    }
}
