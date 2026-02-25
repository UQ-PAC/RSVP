package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class ActionDefinition implements SchemaFileEntry {

    public static class ActionReference {
        private final String id;
        private final String type;

        protected ActionReference(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }

    private static class ActionApplication {

        @SerializedName("principalTypes")
        private final Set<String> unresolvedPrincipalTypes;

        @SerializedName("resourceTypes")
        private final Set<String> unresolvedResourceTypes;

        private final RecordTypeDefinition context;

        // Resolved principal type references
        private Set<EntityTypeDefinition> resolvedPrincipalDefinitions;

        // Resolved resource type references
        private Set<EntityTypeDefinition> resolvedResourceDefinitions;

        private ActionApplication(Set<String> principalTypes, Set<String> resourceTypes, RecordTypeDefinition context) {
            this.unresolvedPrincipalTypes = principalTypes != null ? Set.copyOf(principalTypes)
                    : Collections.emptySet();
            this.unresolvedResourceTypes = resourceTypes != null ? Set.copyOf(resourceTypes) : Collections.emptySet();
            this.context = context;
        }
    }

    @SerializedName("memberOf")
    private final Set<ActionReference> unresolvedMemberOf;

    private final ActionApplication appliesTo;

    private final Map<String, String> annotations;

    // Set during type resolution to the key this action was mapped to in the
    // namespace
    private String name;

    // Set during type resolution
    private Set<ActionDefinition> resolvedMemberOf;

    public ActionDefinition(Set<ActionReference> memberOf, Set<String> principalTypes, Set<String> resourceTypes,
            RecordTypeDefinition context, Map<String, String> annotations) {

        this.unresolvedMemberOf = memberOf != null ? Set.copyOf(memberOf) : Collections.emptySet();
        this.appliesTo = new ActionApplication(principalTypes, resourceTypes, context);
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
    }

    public void resolveReferences(Schema schema, Namespace local) {

        resolvedMemberOf = new HashSet<>();

        if (unresolvedMemberOf != null) {
            for (ActionReference ref : unresolvedMemberOf) {
                ActionDefinition resolved = Schema.resolveActionType(ref.id, ref.type, schema, local);

                if (resolved != null) {
                    resolvedMemberOf.add(resolved);
                } else {
                    unresolvedMemberOf.add(ref);
                }
            }
        }

        appliesTo.resolvedPrincipalDefinitions = new HashSet<>();
        appliesTo.resolvedResourceDefinitions = new HashSet<>();

        if (appliesTo.unresolvedPrincipalTypes != null) {
            for (String entityType : appliesTo.unresolvedPrincipalTypes) {
                EntityTypeDefinition resolved = Schema.resolveEntityType(entityType, schema, local);

                if (resolved != null) {
                    appliesTo.resolvedPrincipalDefinitions.add(resolved);
                } else {
                    appliesTo.unresolvedPrincipalTypes.add(entityType);
                }
            }
        }

        if (appliesTo.unresolvedResourceTypes != null) {
            for (String entityType : appliesTo.unresolvedResourceTypes) {
                EntityTypeDefinition resolved = Schema.resolveEntityType(entityType, schema, local);

                if (resolved != null) {
                    appliesTo.resolvedResourceDefinitions.add(resolved);
                } else {
                    appliesTo.unresolvedResourceTypes.add(entityType);
                }
            }
        }

    }

    /**
     * If this action is defined within a resolved namespace, then return the fully
     * qualified name of this action definition in the format
     * {@code Namespace::Action::actionName}
     * 
     * @return The fully qualified name of this action if this action is defined
     *         within a resolved namespace, {@code null} otherwise.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ActionDefinition> getMemberOf() {
        return resolvedMemberOf != null ? Set.copyOf(resolvedMemberOf) : Collections.emptySet();
    }

    public Set<EntityTypeDefinition> getAppliesToPrincipalTypes() {
        return appliesTo.resolvedPrincipalDefinitions != null ? Set.copyOf(appliesTo.resolvedPrincipalDefinitions)
                : Collections.emptySet();
    }

    public Set<EntityTypeDefinition> getAppliesToResourceTypes() {
        return appliesTo.resolvedResourceDefinitions != null ? Set.copyOf(appliesTo.resolvedResourceDefinitions)
                : Collections.emptySet();
    }

    public RecordTypeDefinition getAppliesToContext() {
        return appliesTo.context;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitActionDefinition(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitActionDefinition(this);
    }
}
