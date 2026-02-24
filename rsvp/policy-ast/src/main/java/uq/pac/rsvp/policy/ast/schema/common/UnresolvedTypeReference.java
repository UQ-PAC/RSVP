package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

/**
 * An unresolved type reference
 */
public class UnresolvedTypeReference extends CommonTypeDefinition {

    public UnresolvedTypeReference(String name, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.setName(name);
    }

    public UnresolvedTypeReference(String name) {
        super(true, null);
        this.setName(name);
    }

    @Override
    public void accept(SchemaVisitor visitor) {
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return null;
    }

}
