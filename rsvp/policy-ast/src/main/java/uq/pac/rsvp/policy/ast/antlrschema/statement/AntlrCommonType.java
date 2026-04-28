package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class AntlrCommonType extends AntlrSchemaStatement {

    private final AntlrBuiltinType definition;

    public AntlrCommonType(String namespace, String name, AntlrBuiltinType definition, SourceLoc location) {
        super(namespace, name, location);
        this.definition = definition;
    }

    public AntlrBuiltinType getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "type " + getName() + " = " + definition + ";";
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitCommon(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitCommon(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitCommon(this, payload);
    }
}
