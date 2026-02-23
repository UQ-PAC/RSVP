package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class CommonTypeReference extends CommonTypeDefinition {

    private final CommonTypeDefinition definition;

    public CommonTypeReference(CommonTypeDefinition definition, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.definition = definition;
    }

    public CommonTypeReference(CommonTypeDefinition definition, Map<String, String> annotations) {
        super(annotations);
        this.definition = definition;
    }

    public CommonTypeReference(CommonTypeDefinition definition, boolean required) {
        super(required);
        this.definition = definition;
    }

    public CommonTypeReference(CommonTypeDefinition definition) {
        super();
        this.definition = definition;
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
