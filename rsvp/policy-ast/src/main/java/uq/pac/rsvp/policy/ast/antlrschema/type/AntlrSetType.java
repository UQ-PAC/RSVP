package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrSetType extends AntlrBuiltinType {

    private final AntlrBuiltinType element;

    public AntlrSetType(AntlrBuiltinType element, SourceLoc location) {
        super(location);
        this.element = element;
    }

    public AntlrSetType(AntlrBuiltinType element) {
        this(element, SourceLoc.MISSING);
    }

    public AntlrBuiltinType getElementType() {
        return element;
    }

    @Override
    public String toString() {
        return "Set<" + element + ">";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AntlrSetType set) {
            return set.element.equals(this.element);
        }
        return false;
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitSet(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitSet(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitSet(this, payload);
    }
}
