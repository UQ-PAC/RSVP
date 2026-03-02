package uq.pac.rsvp.policy.datalog.ast;

import com.cedarpolicy.AuthorizationEngine;
import uq.pac.rsvp.policy.datalog.translation.RequestAuth;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Datalog program as an ordered list of statements
 */
public class DLProgram extends DLNode {
    private final List<DLStatement> statements;

    public DLProgram(List<DLStatement> statements) {
        this.statements = statements.stream().toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String stringify() {
        return String.join("\n", statements.stream().map(DLStatement::toString).toList());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLProgram p) {
            return p.statements.equals(statements);
        }
        return false;
    }

    Stream<DLStatement> statements() {
        return statements.stream();
    }

    public static class Builder {
        private final List<DLStatement> statements;

        public Builder() {
            this.statements = new ArrayList<>();
        }

        public Builder add(DLStatement stmt) {
            statements.add(stmt);
            return this;
        }

        public Builder comment(String msg) {
            add(new DLComment(msg));
            return this;
        }

        public Builder nlComment(String msg) {
            space();
            add(new DLComment(msg));
            return this;
        }

        public Builder add(Collection<? extends DLStatement> stmts) {
            statements.addAll(stmts);
            return this;
        }

        public Builder space() {
            return comment("");
        }

        public Builder fact(String relation, DLTerm ...terms) {
            return add(new DLFact(new DLAtom(relation, Arrays.stream(terms).toList())));
        }

        public DLProgram build() {
            return new DLProgram(statements);
        }

        public Builder reset() {
            statements.clear();
            return this;
        }
    }

    public RequestAuth execute() throws IOException, InterruptedException {
        // FIXME: Temporary directory
        Path baseDir = Path.of("/tmp/rsvp_dl").toAbsolutePath();

        Files.createDirectories(baseDir);
        Path authDl = Path.of(baseDir.toString(), "auth.dl");

        Files.writeString(authDl, stringify());

        ProcessBuilder builder = new ProcessBuilder();

        Process process = builder.directory(baseDir.toFile())
                .command("souffle", "-j", "auto", authDl.toString())
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            BufferedReader error =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorString = error.lines().collect(Collectors.joining("\n"));
            throw new TranslationError(errorString);
        } else {
            return RequestAuth.load(baseDir);
        }
    }
}
