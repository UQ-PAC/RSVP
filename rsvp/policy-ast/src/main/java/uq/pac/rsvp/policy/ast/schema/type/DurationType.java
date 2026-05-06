package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class DurationType extends BuiltinType {

    public DurationType(SourceLoc location) {
        super(location);
    }

    public DurationType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DurationType;
    }

    @Override
    public String toString() {
        return "__cedar::duration";
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitDuration(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitDuration(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitDuration(this, payload);
    }
}
