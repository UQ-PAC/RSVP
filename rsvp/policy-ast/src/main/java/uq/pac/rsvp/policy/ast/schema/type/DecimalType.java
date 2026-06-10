package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class DecimalType extends BuiltinType {

    public DecimalType(SourceLoc location) {
        super(location);
    }

    public DecimalType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "__cedar::decimal";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DecimalType;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitDecimal(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitDecimal(this);
    }

    @Override
    public <T> void accept(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitDecimal(this, payload);
    }
}
