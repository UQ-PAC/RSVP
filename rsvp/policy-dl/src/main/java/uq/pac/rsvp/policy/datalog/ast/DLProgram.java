package uq.pac.rsvp.policy.datalog.ast;

import uq.pac.rsvp.policy.datalog.translation.RequestAuth;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
        return execute(null);
    }

    private static void removeTempDir(Path dir) throws IOException {
        if (dir == null) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            for (Path path : files.toList()) {
                Files.delete(path);
            }
        }
        Files.delete(dir);
    }

    public RequestAuth execute(Path dir) throws IOException, InterruptedException {
        Path baseDir = dir;
        Path removeDir = null;
        if (dir == null) {
            baseDir = Files.createTempDirectory("rsvp");
            removeDir = baseDir;
        } else {
            Files.createDirectories(baseDir);
        }

        Files.createDirectories(baseDir);
        Path authDl = Path.of(baseDir.toString(), "auth.dl");

        Files.writeString(authDl, stringify());

        ProcessBuilder builder = new ProcessBuilder();

        Process process = builder.directory(baseDir.toFile())
                .command("souffle", "-j", "auto", authDl.toString())
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            removeTempDir(removeDir);
            BufferedReader error =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            throw new TranslationError(error.lines().collect(Collectors.joining("\n")));
        } else {
            RequestAuth auth = RequestAuth.load(baseDir);
            removeTempDir(removeDir);
            return auth;
        }
    }
}
