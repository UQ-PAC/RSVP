package uq.pac.rsvp.policy.ast.antlrschema;

import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrSchemaStatement;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;

import java.lang.module.ResolutionException;
import java.util.*;
import java.util.stream.Stream;

public class AntlrSchema {

    private final Map<AntlrTypeReference, AntlrSchemaStatement> statements;

    public AntlrSchema(List<AntlrSchemaStatement> statements) {
        this.statements = validate(statements);
    }

    public AntlrSchemaStatement get(AntlrTypeReference ref) {
        return statements.get(ref);
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

    private static Map<AntlrTypeReference, AntlrSchemaStatement> validate(List<AntlrSchemaStatement> statements) {
        // Sort the list of statements such that entities from the global namespace come first
        statements = statements.stream()
                .sorted(Comparator.comparingInt(a -> a.getNamespace().length()))
                .toList();

        Map<AntlrTypeReference, AntlrSchemaStatement> types = new HashMap<>();
        for (AntlrSchemaStatement stmt : statements) {
            AntlrTypeReference ref = stmt.getReference();
            // Try and lookup the reference by nane or by basename in case it exists in the global namespace
            AntlrSchemaStatement lookup = types.containsKey(ref) ?
                    types.get(ref) : types.get(new AntlrTypeReference("", ref.getBaseName()));
            if (lookup != null) {
                throw new ResolutionException("Reference %s illegally shadowed "
                        .formatted(lookup.getReference()));
            }
            types.put(ref, stmt);
        }
        return Collections.unmodifiableMap(types);
    }


}
