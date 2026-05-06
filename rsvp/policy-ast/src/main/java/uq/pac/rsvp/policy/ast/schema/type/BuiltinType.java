package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.SchemaAstNode;
import uq.pac.rsvp.support.SourceLoc;

public abstract class BuiltinType extends SchemaAstNode {
    protected BuiltinType(SourceLoc location) {
        super(location);
    }
}
