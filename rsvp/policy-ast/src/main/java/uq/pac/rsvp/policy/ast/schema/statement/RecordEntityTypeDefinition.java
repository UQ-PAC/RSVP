package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;

public class RecordEntityTypeDefinition extends EntityTypeDefinition {

    private final RecordType shape;

    public RecordEntityTypeDefinition(TypeReference ref, Collection<TypeReference> memberOf, RecordType shape, Annotations annotations, SourceLoc location) {
        super(ref, memberOf, annotations, location);
        this.shape = shape;
    }

    public RecordType getShape() {
        return shape;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitRecordEntity(this);
    }

    @Override
    public <T> T compute(SchemaValueVisitor<T> visitor) {
        return visitor.visitRecordEntity(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitRecordEntity(this, payload);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String in = "";
        if (!getMemberOf().isEmpty()) {
            in = " in " + getMemberOf().stream().map(TypeReference::getName).toList();
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
