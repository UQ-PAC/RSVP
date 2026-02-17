package uq.pac.rsvp.policy.ast.schema.attribute;

import java.util.Map;

import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class SetType extends AttributeType {

    private AttributeType element;

    public SetType(AttributeType element, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.element = element;
    }

    public SetType(AttributeType element, Map<String, String> annotations) {
        super(false, annotations);
        this.element = element;
    }

    public SetType(AttributeType element, boolean required) {
        super(required);
        this.element = element;
    }

    public SetType(AttributeType element) {
        this(element, false);
    }

    public AttributeType getElementType() {
        return this.element;
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visitSetAttributeType(this);
    }
}
