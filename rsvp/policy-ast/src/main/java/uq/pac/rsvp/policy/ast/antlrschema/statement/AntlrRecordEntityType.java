package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AntlrRecordEntityType extends AntlrEntityType {

    private final AntlrRecordType shape;
    private final Set<AntlrTypeReference> memberOf;

    public AntlrRecordEntityType(AntlrTypeReference ref, Collection<AntlrTypeReference> memberOf, AntlrRecordType shape, SourceLoc location) {
        super(ref, location);
        this.shape = shape;
        this.memberOf = Collections.unmodifiableSet(new LinkedHashSet<>(memberOf));
    }

    public Set<AntlrTypeReference> getMemberOf() {
        return memberOf;
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
        if (!memberOf.isEmpty()) {
            in = " in " + memberOf.stream().map(AntlrTypeReference::getName).toList();
        }

        sb.append("entity ")
                .append(getBaseName())
                .append(in)
                .append(" ")
                .append(shape)
                .append(";");

        return sb.toString();
    }
}
