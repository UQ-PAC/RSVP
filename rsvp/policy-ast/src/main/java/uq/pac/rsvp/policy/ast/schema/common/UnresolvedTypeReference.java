package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

/**
 * An unresolved type reference
 */
public class UnresolvedTypeReference extends CommonTypeDefinition {

    @SerializedName("name")
    private final String rawTypeName;

    public UnresolvedTypeReference(String definitionName, String typeName, Map<String, String> annotations) {
        super(definitionName, annotations);
        this.rawTypeName = typeName;
    }

    public UnresolvedTypeReference(String typeName, boolean required, Map<String, String> annotations) {
        super(required, annotations);
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
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return null;
    }

}
