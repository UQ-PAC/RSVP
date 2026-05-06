package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class EntityTypeDefinition extends SchemaStatement {

    private final Set<TypeReference> memberOf;

    public EntityTypeDefinition(TypeReference ref, Collection<TypeReference> memberOf, Annotations annotations, SourceLoc location) {
        super(ref, annotations, location);
        this.memberOf = Collections.unmodifiableSet(new LinkedHashSet<>(memberOf));
    }

    public abstract RecordType getShape();

    public final Set<TypeReference> getMemberOf() {
        return memberOf;
    }
}
