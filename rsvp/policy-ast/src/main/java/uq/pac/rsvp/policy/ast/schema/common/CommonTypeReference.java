package uq.pac.rsvp.policy.ast.schema.common;


import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class CommonTypeReference extends CommonTypeDefinition {

    private final CommonTypeDefinition definition;

    public CommonTypeReference(String name, CommonTypeDefinition definition) {
        super(name);
        this.definition = definition;
    }

    public CommonTypeReference(CommonTypeDefinition definition) {
        this(null, definition);
    }

    public CommonTypeReference() {
        this(null, null);
    }

    public CommonTypeDefinition getDefinition() {
        return definition;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitCommonTypeReference(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitCommonTypeReference(this);
    }

}
