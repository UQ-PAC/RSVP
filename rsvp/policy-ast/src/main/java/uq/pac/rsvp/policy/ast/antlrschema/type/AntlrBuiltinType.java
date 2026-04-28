package uq.pac.rsvp.policy.ast.antlrschema.type;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchemaAstNode;
import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrBuiltinType extends AntlrSchemaAstNode {
    protected AntlrBuiltinType(SourceLoc location) {
        super(location);
    }
}
