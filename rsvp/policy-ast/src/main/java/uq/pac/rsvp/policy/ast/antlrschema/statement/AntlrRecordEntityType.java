package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;

public class AntlrRecordEntityType extends AntlrEntityType {

    private final AntlrRecordType shape;

    public AntlrRecordEntityType(AntlrTypeReference ref, Collection<AntlrTypeReference> memberOf, AntlrRecordType shape, AntlrAnnotations annotations, SourceLoc location) {
        super(ref, memberOf, annotations, location);
        this.shape = shape;
    }

    public AntlrRecordType getShape() {
        return shape;
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitRecordEntity(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitRecordEntity(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitRecordEntity(this, payload);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String in = "";
        if (!getMemberOf().isEmpty()) {
            in = " in " + getMemberOf().stream().map(AntlrTypeReference::getName).toList();
        }

        sb.append("entity ")
                .append(getBaseName())
                .append(in)
                .append(" ")
                .append(shape)
                .append(";");

        return getAnnotations().toString() + sb;
    }
}
