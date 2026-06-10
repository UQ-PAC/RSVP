package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class DateTimeType extends BuiltinType {

    public DateTimeType(SourceLoc location) {
        super(location);
    }

    public DateTimeType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "__cedar::datetime";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DateTimeType;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitDateTime(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitDateTime(this);
    }

    @Override
    public <T> void accept(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitDateTime(this, payload);
    }
}
