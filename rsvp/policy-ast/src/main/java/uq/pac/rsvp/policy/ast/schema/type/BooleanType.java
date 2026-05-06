package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class BooleanType extends BuiltinType {

    public BooleanType(SourceLoc location) {
        super(location);
    }

    public BooleanType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "__cedar::Bool";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BooleanType;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitBoolean(this);
    }

    @Override
    public <T> T compute(SchemaValueVisitor<T> visitor) {
        return visitor.visitBoolean(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitBoolean(this, payload);
    }
}
