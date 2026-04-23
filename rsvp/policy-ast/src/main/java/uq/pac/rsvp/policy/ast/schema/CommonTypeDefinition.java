package uq.pac.rsvp.policy.ast.schema;

import uq.pac.rsvp.policy.ast.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;

public abstract class CommonTypeDefinition implements SchemaItem {

    private final String definitionName;

    // If this type definition is a record property, this may be true. Otherwise,
    // it will be false
    private final boolean required;

    protected CommonTypeDefinition(String name, boolean required) {
        this.definitionName = name;
        this.required = required;
    }

    protected CommonTypeDefinition(String name) {
        this(name, false);
    }

    protected CommonTypeDefinition(boolean required) {
        this(null, required);
    }

    protected CommonTypeDefinition() {
        this(null, false);
    }

    public boolean isRequired() {
        return required;
    }

    /**
     * If this type represents a {@code commonType} definition, then
     * return the fully qualified name of this type definition in the format
     * {@code Namespace::TypeName}. A definition is a {@code commonType} if it is
     * defined in the {@code commonTypes} array within a namespace, not as a
     * component of some other type.
     * 
     * @return The fully qualified name of this type if this type is defined within
     *         a resolved namespace, {@code null} otherwise.
     */
    public final String getName() {
        return definitionName;
    }

    public final boolean hasName() {
        return definitionName != null;
    }

    @Override
    public abstract void accept(SchemaVisitor visitor);

    @Override
    public abstract <T> T compute(SchemaComputationVisitor<T> visitor);

    @Override
    public abstract <T> void process(SchemaPayloadVisitor<T> visitor, T payload);

}
