package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrDecimalType extends AntlrBuiltinType {

    public AntlrDecimalType(SourceLoc location) {
        super(location);
    }

    public AntlrDecimalType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "decimal";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AntlrDecimalType;
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitDecimal(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitDecimal(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitDecimal(this, payload);
    }
}
