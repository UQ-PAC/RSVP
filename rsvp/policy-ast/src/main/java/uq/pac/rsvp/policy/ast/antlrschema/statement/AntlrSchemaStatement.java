package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchemaAstNode;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrSchemaStatement extends AntlrSchemaAstNode {

    private final AntlrTypeReference reference;

    public AntlrSchemaStatement(AntlrTypeReference reference, SourceLoc location) {
        super(location);
        this.reference = reference;
    }

    public AntlrTypeReference getReference() {
        return reference;
    }

    public String getName() {
        return reference.getName();
    }

    public String getNamespace() {
        return reference.getNamespace();
    }

    public String getBaseName() {
        return reference.getBaseName();
    }
}
