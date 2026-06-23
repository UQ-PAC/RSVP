/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents a collection of policy statements, such as cedar policies and invariants
 */
public class PolicyProgram {
    private final List<PolicyStatement> statements;

    private PolicyProgram(Collection<PolicyStatement> statements) {
        this.statements = List.copyOf(statements);
    }

    public Collection<PolicyStatement> getStatements() {
        return statements;
    }

    public Collection<Invariant> getInvariants() {
        return invariants().toList();
    }

    public Collection<Policy> getPolicies() {
        return policies().toList();
    }

    public Stream<PolicyStatement> stream() {
        return statements.stream();
    }

    public Stream<Invariant> invariants() {
        return statements.stream()
                .filter(s -> s instanceof Invariant)
                .map(s -> (Invariant) s);
    }

    public Stream<Policy> policies() {
        return statements.stream()
                .filter(s -> s instanceof Policy)
                .map(s -> (Policy) s);
    }

    public static PolicyProgram parse(Path file) throws IOException {
        return parse(file.toString(), Files.readString(file));
    }

    public static PolicyProgram parse(String text) {
        return parse("unknown", text);
    }

    public static PolicyProgram parse(String file, String text) {
        return new PolicyProgram(PolicyParser.parse(file, text));
    }

    public static PolicyProgram of(Collection<PolicyStatement> statements) {
        return new PolicyProgram(statements);
    }
}
