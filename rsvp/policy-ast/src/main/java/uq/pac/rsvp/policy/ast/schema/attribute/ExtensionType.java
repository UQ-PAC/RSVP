package uq.pac.rsvp.policy.ast.schema.attribute;

import java.util.Map;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public class ExtensionType extends AttributeType {

    private final String name;

    public ExtensionType(String name, boolean required, Map<String, String> annotations) {
        super(required, annotations);
        this.name = name;
    }

    public ExtensionType(String name, Map<String, String> annotations) {
        super(false, annotations);
        this.name = name;
    }

    public ExtensionType(String name, boolean required) {
        super(required);
        this.name = name;
    }

    public ExtensionType(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitExtensionAttributeType(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitExtensionAttributeType(this);
    }
}
