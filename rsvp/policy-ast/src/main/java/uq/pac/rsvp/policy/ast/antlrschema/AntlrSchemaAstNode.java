package uq.pac.rsvp.policy.ast.antlrschema;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrSchemaAstNode extends AstNode {
    public AntlrSchemaAstNode(SourceLoc location) {
        super(location);
    }

    public abstract void accept(AntlrSchemaVisitor visitor);

    public abstract <T> T compute(AntlrSchemaValueVisitor<T> visitor);

    public abstract <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload);
}
