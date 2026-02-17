package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class Action {

    private static class ActionReference {
        private String id;
        private String type;
    }

    private static class ActionApplication {
        private Set<String> principalTypes;
        private Set<String> resourceTypes;
        private RecordType context;

        private Set<EntityType> principalTypeRefs;
        private Set<EntityType> resourceTypeRefs;

        private Set<String> unresolvedPrincipalTypes;
        private Set<String> unresolvedResourceTypes;

        private ActionApplication(Set<String> principalTypes, Set<String> resourceTypes,
                RecordType context) {
            this.principalTypes = principalTypes != null ? new HashSet<>(principalTypes) : Collections.emptySet();
            this.resourceTypes = resourceTypes != null ? new HashSet<>(resourceTypes) : Collections.emptySet();
            this.context = context;
        }
    }

    private String name;

    private Set<ActionReference> memberOf;
    private ActionApplication appliesTo;
    private Map<String, String> annotations;

    private Set<Action> memberOfReferences;
    private Set<ActionReference> unresolvedMemberOf;

    public Action(Set<ActionReference> memberOf, Set<String> principalTypes, Set<String> resourceTypes,
            RecordType context, Map<String, String> annotations) {

        this.memberOf = memberOf != null ? new HashSet<>(memberOf) : Collections.emptySet();
        this.appliesTo = new ActionApplication(principalTypes, resourceTypes, context);
        this.annotations = annotations != null ? new HashMap<>(annotations) : Collections.emptyMap();
    }

    public void resolveReferences(Schema schema, Namespace local) {

        memberOfReferences = new HashSet<>();
        unresolvedMemberOf = new HashSet<>();

        if (memberOf != null) {
            for (ActionReference ref : memberOf) {
                Action resolved = Schema.resolveActionType(ref.id, ref.type, schema, local);

                if (resolved != null) {
                    memberOfReferences.add(resolved);
                } else {
                    unresolvedMemberOf.add(ref);
                }
            }
        }

        appliesTo.principalTypeRefs = new HashSet<>();
        appliesTo.unresolvedPrincipalTypes = new HashSet<>();
        appliesTo.resourceTypeRefs = new HashSet<>();
        appliesTo.unresolvedResourceTypes = new HashSet<>();

        if (appliesTo.principalTypes != null) {
            for (String entityType : appliesTo.principalTypes) {
                EntityType resolved = Schema.resolveEntityType(entityType, schema, local);

                if (resolved != null) {
                    appliesTo.principalTypeRefs.add(resolved);
                } else {
                    appliesTo.unresolvedPrincipalTypes.add(entityType);
                }
            }
        }

        if (appliesTo.resourceTypes != null) {
            for (String entityType : appliesTo.resourceTypes) {
                EntityType resolved = Schema.resolveEntityType(entityType, schema, local);

                if (resolved != null) {
                    appliesTo.resourceTypeRefs.add(resolved);
                } else {
                    appliesTo.unresolvedResourceTypes.add(entityType);
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

    public Set<Action> getMemberOfActions() {
        return memberOfReferences != null ? new HashSet<>(memberOfReferences) : Collections.emptySet();
    }

    public Set<EntityType> getAppliesToPrincipalTypes() {
        return appliesTo.principalTypeRefs != null ? new HashSet<>(appliesTo.principalTypeRefs)
                : Collections.emptySet();
    }

    public Set<EntityType> getAppliesToResourceTypes() {
        return appliesTo.resourceTypeRefs != null ? new HashSet<>(appliesTo.resourceTypeRefs) : Collections.emptySet();
    }

    public RecordType getAppliesToContext() {
        return appliesTo.context;
    }

    public Map<String, String> getAnnotations() {
        return annotations != null ? new HashMap<>(annotations) : Collections.emptyMap();
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visitAction(this);
    }
}
