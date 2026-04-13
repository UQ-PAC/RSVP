package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

/**
 * An unresolved type reference
 */
public class UnresolvedTypeReference extends CommonTypeDefinition {

    @SerializedName("name")
    private final String rawTypeName;

    public UnresolvedTypeReference(String definitionName, String typeName) {
        super(definitionName);
        this.rawTypeName = typeName;
    }

    public UnresolvedTypeReference(String typeName, boolean required) {
        super(required);
        this.rawTypeName = typeName;
    }

    public UnresolvedTypeReference(String typeName) {
        super();
        this.rawTypeName = typeName;
    }

    public UnresolvedTypeReference() {
        super();
        this.rawTypeName = null;
    }

    public String getRawTypeName() {
        return rawTypeName;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitUnresolvedTypeReference(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitUnresolvedTypeReference(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitUnresolvedTypeReference(this, payload);
    }

    @Override
    public String toString() {
        return "::unresolved reference::";
    }
}
