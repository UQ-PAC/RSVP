package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class StringType extends BuiltinType {

    public StringType(SourceLoc location) {
        super(location);
    }

    public StringType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "__cedar::String";
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitString(this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StringType;
    }

    @Override
    public <T> T compute(SchemaValueVisitor<T> visitor) {
        return visitor.visitString(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitString(this, payload);
    }
}
