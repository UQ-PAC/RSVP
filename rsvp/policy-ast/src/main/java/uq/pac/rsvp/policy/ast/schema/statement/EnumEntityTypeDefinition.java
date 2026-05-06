package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumEntityTypeDefinition extends EntityTypeDefinition {
    private final Set<String> names;

    public EnumEntityTypeDefinition(TypeReference ref, Set<TypeReference> memberOf,
                                    Collection<String> names, Annotations annotations, SourceLoc location) {
        super(ref, memberOf, annotations, location);
        this.names = Collections.unmodifiableSet(new LinkedHashSet<>(names));
    }

    public Set<String> getEnumNames() {
        return names;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitEnumEntity(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitEnumEntity(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
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
    public RecordType getShape() {
        return new RecordType(Collections.emptyMap(), SourceLoc.MISSING);
    }
}
