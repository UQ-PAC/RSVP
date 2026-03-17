package uq.pac.rsvp.policy.datalog.ast;

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
    private final String name;
    private final List<DLStatement> statements;

    public DLProgram(String name, List<DLStatement> statements) {
        this.name = name;
        this.statements = statements.stream().toList();
    }

    public DLProgram(List<DLStatement> statements) {
        this(null, statements);
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

    public Stream<DLStatement> statements() {
        return statements.stream();
    }

    public static class Builder {
        private final List<DLStatement> statements;
        private final String name;

        public Builder(String name) {
            this.statements = new ArrayList<>();
            this.name = name;
        }

        public Builder add(DLStatement stmt) {
            statements.add(stmt);
            return this;
        }

        public Builder comment(String msg) {
            if (!statements.isEmpty()) {
                add(new DLComment(""));
            }
            add(new DLComment(msg));
            return this;
        }

        public Builder add(Collection<? extends DLStatement> stmts) {
            statements.addAll(stmts);
            return this;
        }

        public Builder fact(DLRuleDecl decl, DLTerm ...terms) {
            return add(new DLFact(decl, terms));
        }

        public DLProgram build() {
            return new DLProgram(name, statements);
        }
    }

    public String getName() {
        return name;
    }

    public void execute(Path baseDir) throws IOException, InterruptedException {
        String name = this.getName();
        if (name == null || name.isEmpty()) {
            throw new TranslationError("Unnamed program cannot be executed");
        }

        Files.createDirectories(baseDir);
        Path programDl = Path.of(name.endsWith( ".dl") ? name : name + ".dl");
        Files.writeString(Path.of(baseDir.toString(), programDl.toString()), stringify());
        ProcessBuilder builder = new ProcessBuilder();

        Process process = builder.directory(baseDir.toFile())
                .command("souffle", "-j", "auto", programDl.toString())
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            BufferedReader error =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            throw new TranslationError(error.lines().collect(Collectors.joining("\n")));
        }
    }
}
