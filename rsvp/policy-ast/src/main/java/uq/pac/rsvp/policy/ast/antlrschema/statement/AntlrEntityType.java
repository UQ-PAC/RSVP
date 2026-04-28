package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrEntityType extends AntlrSchemaStatement {

    public AntlrEntityType(String namespace, String name, SourceLoc location) {
        super(namespace, name, location);
    }
}
