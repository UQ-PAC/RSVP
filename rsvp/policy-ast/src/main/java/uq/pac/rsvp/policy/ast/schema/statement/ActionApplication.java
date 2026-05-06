package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static uq.pac.rsvp.Assertion.require;

public class ActionApplication {

    private final Set<TypeReference> principalTypes;
    private final Set<TypeReference> resourceTypes;
    private final RecordType context;

    public ActionApplication(Collection<TypeReference> principal, Collection<TypeReference> resource, RecordType context) {
        this.principalTypes = Collections.unmodifiableSet(new LinkedHashSet<>(principal));
        this.resourceTypes = Collections.unmodifiableSet(new LinkedHashSet<>(resource));
        this.context = context;

        // If principal types are omitted, so are context and resources
        if (principalTypes.isEmpty()) {
            require(resourceTypes.isEmpty());
            require(context.isEmpty());
        } else {
            // If principal types are specified then resource types should be as well
            // albeit context can still be empty
            require(!resourceTypes.isEmpty());
        }

        // If context is specified, sho should be principal and resource types
        if (!context.isEmpty()) {
            require(!principalTypes.isEmpty());
        }
    }

    public Set<TypeReference> getResourceTypes() {
        return resourceTypes;
    }

    public Set<TypeReference> getPrincipalTypes() {
        return principalTypes;
    }

    public RecordType getContext() {
        return context;
    }

    public boolean isEmpty() {
        return principalTypes.isEmpty();
    }

    public String toString() {
        String ctx = "{ }";
        if (!context.isEmpty()) {
            ctx = String.join("\n    ", context.toString().split("\\n"));
        }
        return isEmpty() ? "" : """
                appliesTo {
                    principal: %s,
                    resource: %s,
                    context: %s
                }""".formatted(principalTypes, resourceTypes, ctx);
    }

}
