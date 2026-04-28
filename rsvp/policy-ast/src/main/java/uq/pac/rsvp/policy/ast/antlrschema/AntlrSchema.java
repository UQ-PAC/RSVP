package uq.pac.rsvp.policy.ast.antlrschema;

import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrSchemaStatement;

import java.util.*;
import java.util.stream.Stream;

import static uq.pac.rsvp.Assertion.require;

public class AntlrSchema {

    private final Map<String, AntlrSchemaStatement> statements;

    public AntlrSchema(Map<String, AntlrSchemaStatement> statements) {
        this.statements = Map.copyOf(statements);
        statements.forEach((k, v) -> require(k.equals(v.getName())));
    }

    public AntlrSchemaStatement get(String name) {
        return statements.get(name);
    }

    public Stream<AntlrSchemaStatement> statements() {
        return statements.values().stream();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Map<String, List<AntlrSchemaStatement>> namespaces = new HashMap<>();
        statements.values().forEach(v -> {
            namespaces.computeIfAbsent(v.getNamespace(), k -> new ArrayList<>()).add(v);
        });

        namespaces.forEach((ns, stmts) -> {
            if (!ns.isEmpty()) {
                sb.append("namespace ").append(ns).append(" { ").append("\n");
            }
            stmts.forEach(stmt -> {
                String prefix = ns.isEmpty() ? "" : "    ";
                Arrays.stream(stmt.toString().split("\\n")).forEach(line -> {
                    sb.append(prefix).append(line).append("\n");
                });
            });
            if (!ns.isEmpty()) {
                sb.append("}").append("\n");
            }
        });
        return sb.toString();
    }
}
