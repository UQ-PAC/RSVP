package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AntlrEntityType extends AntlrSchemaStatement {

    private final Set<AntlrTypeReference> memberOf;

    public AntlrEntityType(AntlrTypeReference ref, Collection<AntlrTypeReference> memberOf, AntlrAnnotations annotations, SourceLoc location) {
        super(ref, annotations, location);
        this.memberOf = Collections.unmodifiableSet(new LinkedHashSet<>(memberOf));
    }

    public abstract AntlrRecordType getShape();

    public final Set<AntlrTypeReference> getMemberOf() {
        return memberOf;
    }
}
