package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class CommonType extends SchemaStatement {

    private final BuiltinType definition;

    public CommonType(TypeReference ref, BuiltinType definition, Annotations annotations, SourceLoc location) {
        super(ref, annotations, location);
        this.definition = definition;
    }

    public BuiltinType getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return getAnnotations().toString() + "type " + getBaseName() + " = " + definition + ";";
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitCommon(this);
    }

    @Override
    public <T> T compute(SchemaValueVisitor<T> visitor) {
        return visitor.visitCommon(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitCommon(this, payload);
    }
}
