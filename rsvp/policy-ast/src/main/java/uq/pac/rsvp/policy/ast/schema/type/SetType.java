package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class SetType extends BuiltinType {

    private final BuiltinType element;

    public SetType(BuiltinType element, SourceLoc location) {
        super(location);
        this.element = element;
    }

    public SetType(BuiltinType element) {
        this(element, SourceLoc.MISSING);
    }

    public BuiltinType getElementType() {
        return element;
    }

    @Override
    public String toString() {
        return "Set<" + element + ">";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SetType set) {
            return set.element.equals(this.element);
        }
        return false;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitSet(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitSet(this);
    }

    @Override
    public <T> void accept(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitSet(this, payload);
    }
}
