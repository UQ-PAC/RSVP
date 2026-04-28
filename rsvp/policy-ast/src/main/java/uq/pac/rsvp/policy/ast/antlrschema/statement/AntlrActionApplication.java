package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;

import java.util.Set;

import static uq.pac.rsvp.Assertion.require;

public class AntlrActionApplication {

    private final Set<AntlrTypeReference> principalTypes;
    private final Set<AntlrTypeReference> resourceTypes;
    private final AntlrRecordType context;

    public AntlrActionApplication(Set<AntlrTypeReference> principal, Set<AntlrTypeReference> resource, AntlrRecordType context) {
        this.principalTypes = Set.copyOf(principal);
        this.resourceTypes = Set.copyOf(resource);
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

    public Set<AntlrTypeReference> getResourceTypes() {
        return resourceTypes;
    }

    public Set<AntlrTypeReference> getPrincipalTypes() {
        return principalTypes;
    }

    public AntlrRecordType getContext() {
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
