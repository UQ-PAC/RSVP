package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchemaAstNode;
import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrSchemaStatement extends AntlrSchemaAstNode {

    private final String namespace;
    private final String name;

    public AntlrSchemaStatement(String namespace, String name, SourceLoc location) {
        super(location);
        this.namespace = namespace;
        this.name = name;
    }

    public String getBaseName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return namespace.isEmpty() ? name : namespace + "::" + name;
    }
}
