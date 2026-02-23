package uq.pac.rsvp.policy.ast.schema.common;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class SetTypeDefinition extends CommonTypeDefinition {

    private CommonTypeDefinition element;

    public SetTypeDefinition(CommonTypeDefinition element, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.element = element;
    }

    public SetTypeDefinition(CommonTypeDefinition element, Map<String, String> annotations) {
        super(false, annotations);
        this.element = element;
    }

    public SetTypeDefinition(CommonTypeDefinition element, boolean required) {
        super(required);
        this.element = element;
    }

    public SetTypeDefinition(CommonTypeDefinition element) {
        this(element, false);
    }

    public CommonTypeDefinition getElementType() {
        return this.element;
    }

    public void resolveElementType(CommonTypeDefinition type) {
        this.element = type;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitSetTypeDefinition(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitSetTypeDefinition(this);
    }
}
