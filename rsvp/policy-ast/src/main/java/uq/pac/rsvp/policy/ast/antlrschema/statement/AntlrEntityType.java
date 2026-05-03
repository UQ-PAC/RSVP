package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrEntityType extends AntlrSchemaStatement {
    public AntlrEntityType(AntlrTypeReference ref, SourceLoc location) {
        super(ref, location);
    }
}
