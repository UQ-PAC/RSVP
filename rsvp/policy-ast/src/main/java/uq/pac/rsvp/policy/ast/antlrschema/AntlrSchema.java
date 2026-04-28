package uq.pac.rsvp.policy.ast.antlrschema;

import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrSchemaStatement;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;

public class AntlrSchema {

    private final Map<String, AntlrSchemaStatement> statements;

    public AntlrSchema(Collection<AntlrSchemaStatement> statements) {
        this.statements = statements.stream()
                .collect(Collectors.toUnmodifiableMap(AntlrSchemaStatement::getName, v -> v));
        require(statements.size() == this.statements.size());
    }

    public AntlrSchemaStatement get(String name) {
        return statements.get(name);
    }
}
