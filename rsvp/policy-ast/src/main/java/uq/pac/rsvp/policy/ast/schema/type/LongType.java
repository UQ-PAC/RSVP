package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class LongType extends BuiltinType {

    public LongType(SourceLoc location) {
        super(location);
    }

    public LongType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LongType;
    }

    @Override
    public String toString() {
        return "__cedar::Long";
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitLong(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitLong(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitLong(this, payload);
    }
}
