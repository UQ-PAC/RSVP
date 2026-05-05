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
import java.util.stream.Collectors;

public class AntlrEnumEntityType extends AntlrEntityType {
    private final Set<String> names;

    public AntlrEnumEntityType(AntlrTypeReference ref, Set<AntlrTypeReference> memberOf,
                               Collection<String> names, AntlrAnnotations annotations, SourceLoc location) {
        super(ref, memberOf, annotations, location);
        this.names = Collections.unmodifiableSet(new LinkedHashSet<>(names));
    }

    public Set<String> getEnumNames() {
        return names;
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitEnumEntity(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitEnumEntity(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitEnumEntity(this, payload);
    }

    @Override
    public String toString() {
        String values = names.stream()
                .map(n -> '"' + n + '"')
                .collect(Collectors.joining(", "));
        return getAnnotations().toString() + "entity %s enum [ %s ];".formatted(getBaseName(), values);
    }

    @Override
    public AntlrRecordType getShape() {
        return new AntlrRecordType(Collections.emptyMap(), SourceLoc.MISSING);
    }
}
