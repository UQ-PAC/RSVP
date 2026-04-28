package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrDateTimeType extends AntlrBuiltinType {

    public AntlrDateTimeType(SourceLoc location) {
        super(location);
    }

    public AntlrDateTimeType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "datetime";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AntlrDateTimeType;
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitDateTime(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitDateTime(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitDateTime(this, payload);
    }
}
