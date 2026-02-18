package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class Action {

    private static class ActionReference {
        private final String id;
        private final String type;

        protected ActionReference(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }

    private static class ActionApplication {
        private final Set<String> principalTypes;
        private final Set<String> resourceTypes;
        private final RecordType context;

        // Resolved principal type references
        private Set<EntityType> principalTypeRefs;

        // Resolved resource type references
        private Set<EntityType> resourceTypeRefs;

        // Any unresolved principal types
        private Set<String> unresolvedPrincipalTypes;

        // Any unresolved resource types
        private Set<String> unresolvedResourceTypes;

        private ActionApplication(Set<String> principalTypes, Set<String> resourceTypes, RecordType context) {
            this.principalTypes = principalTypes != null ? Set.copyOf(principalTypes) : Collections.emptySet();
            this.resourceTypes = resourceTypes != null ? Set.copyOf(resourceTypes) : Collections.emptySet();
            this.context = context;
        }
    }

    private final Set<ActionReference> memberOf;
    private final ActionApplication appliesTo;
    private final Map<String, String> annotations;

    // The name that this Action is mapped to within the namespace
    private String name;

    // Resolved memberOf Action references
    private Set<Action> memberOfReferences;

    // Any unresolved memberOf Actions
    private Set<ActionReference> unresolvedMemberOf;

    public Action(Set<ActionReference> memberOf, Set<String> principalTypes, Set<String> resourceTypes,
            RecordType context, Map<String, String> annotations) {

        this.memberOf = memberOf != null ? Set.copyOf(memberOf) : Collections.emptySet();
        this.appliesTo = new ActionApplication(principalTypes, resourceTypes, context);
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
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
        return memberOfReferences != null ? Set.copyOf(memberOfReferences) : Collections.emptySet();
    }

    public Set<EntityType> getAppliesToPrincipalTypes() {
        return appliesTo.principalTypeRefs != null ? Set.copyOf(appliesTo.principalTypeRefs)
                : Collections.emptySet();
    }

    public Set<EntityType> getAppliesToResourceTypes() {
        return appliesTo.resourceTypeRefs != null ? Set.copyOf(appliesTo.resourceTypeRefs) : Collections.emptySet();
    }

    public RecordType getAppliesToContext() {
        return appliesTo.context;
    }

    public Map<String, String> getAnnotations() {
        return annotations != null ? annotations : Collections.emptyMap();
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visitAction(this);
    }

    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitAction(this);
    }
}
