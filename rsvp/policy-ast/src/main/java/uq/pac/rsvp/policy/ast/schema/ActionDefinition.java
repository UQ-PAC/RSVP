package uq.pac.rsvp.policy.ast.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class ActionDefinition implements SchemaItem {

    public static class ActionReference {
        private final String id;
        private final String type;

        protected ActionReference(String id, String type) {
            this.id = id;
            this.type = type;
        }

        protected ActionReference() {
            this.id = null;
            this.type = null;
        }

        public String getName() {
            return "%s::%s".formatted(type, id);
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

        private ActionApplication() {
            this.unresolvedPrincipalTypes = Collections.emptySet();
            this.unresolvedResourceTypes = Collections.emptySet();
            this.context = null;
        }
    }

    private final String name;

    @SerializedName("memberOf")
    private final Set<ActionReference> unresolvedMemberOf;

    private final ActionApplication appliesTo;

    private final Map<String, String> annotations;

    // Set during type resolution
    private Set<ActionDefinition> resolvedMemberOf;

    public ActionDefinition(String name, Set<ActionReference> memberOf, Set<String> principalTypes,
            Set<String> resourceTypes,
            RecordTypeDefinition context, Map<String, String> annotations) {
        this.name = name;
        this.unresolvedMemberOf = memberOf != null ? Set.copyOf(memberOf) : Collections.emptySet();
        this.appliesTo = new ActionApplication(principalTypes, resourceTypes, context);
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
    }

    public ActionDefinition(String name) {
        this(name, null, null, null, null, null);
    }

    public ActionDefinition() {
        this(null, null, null, null, null, null);
    }

    public void resolveReferences(Schema schema, Namespace local) {

        resolvedMemberOf = new HashSet<>();

        for (ActionReference ref : unresolvedMemberOf) {
            ActionDefinition resolved = Schema.resolveActionType(ref.id, ref.type, schema, local);

            if (resolved != null) {
                resolvedMemberOf.add(resolved);
            }
        }

        appliesTo.resolvedPrincipalDefinitions = new HashSet<>();
        appliesTo.resolvedResourceDefinitions = new HashSet<>();

        for (String entityType : appliesTo.unresolvedPrincipalTypes) {
            EntityTypeDefinition resolved = Schema.resolveEntityType(entityType, schema, local);

            if (resolved != null) {
                appliesTo.resolvedPrincipalDefinitions.add(resolved);
            }
        }

        for (String entityType : appliesTo.unresolvedResourceTypes) {
            EntityTypeDefinition resolved = Schema.resolveEntityType(entityType, schema, local);

            if (resolved != null) {
                appliesTo.resolvedResourceDefinitions.add(resolved);
            }
        }

    }

    /**
     * Return the fully qualified name of this action definition in the format
     * {@code Namespace::Action::actionName}
     * 
     * @return The fully qualified name of this action if this action is defined
     *         within a resolved namespace, {@code null} otherwise.
     */
    public String getName() {
        return name;
    }

    public Set<ActionDefinition> getMemberOf() {
        return resolvedMemberOf != null ? Set.copyOf(resolvedMemberOf) : Collections.emptySet();
    }

    public Set<ActionReference> getMemberOfReferences() {
        return Set.copyOf(unresolvedMemberOf);
    }

    public Set<EntityTypeDefinition> getAppliesToPrincipalTypes() {
        return appliesTo.resolvedPrincipalDefinitions != null
                ? Set.copyOf(appliesTo.resolvedPrincipalDefinitions)
                : Collections.emptySet();
    }

    public Set<EntityTypeDefinition> getAppliesToResourceTypes() {
        return appliesTo.resolvedResourceDefinitions != null
                ? Set.copyOf(appliesTo.resolvedResourceDefinitions)
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
