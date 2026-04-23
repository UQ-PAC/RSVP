package uq.pac.rsvp.policy.ast;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.invariant.SourceVisitor;
import uq.pac.rsvp.policy.ast.invariant.StatementVisitor;
import uq.pac.rsvp.support.FileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class represents a collection of policy statements, such as cedar policies and invariants
 */
public class PolicyProgram {
    private final List<Statement> statements;

    private PolicyProgram(Collection<Statement> statements) {
        this.statements = List.copyOf(statements);
    }

    public Collection<Statement> getStatements() {
        return statements;
    }

    public Collection<Invariant> getInvariants() {
        return invariants().toList();
    }

    public Collection<Policy> getPolicies() {
        return policies().toList();
    }

    public Stream<Statement> stream() {
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

    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                    String msg, RecognitionException e) {
            throw new ParseCancellationException("Parse error: " + line + ":" + pos + " " + msg);
        }
    }

    public static PolicyProgram parse(Path file) throws IOException {
        return parse(file.toString(), Files.readString(file));
    }

    public static PolicyProgram parse(String text) {
        return parse("unknown", text);
    }

    public static PolicyProgram parse(String file, String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarLexer lexer = new CedarLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarParser parser = new CedarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        FileSource fs = new FileSource(file, text);
        StatementVisitor visitor = new StatementVisitor(fs);
        List<Statement> statements = new SourceVisitor<List<Statement>>(fs) {
            @Override
            public List<Statement> visitProgram(CedarParser.ProgramContext ctx) {
                if (ctx.children != null) {
                    return ctx.children.stream().map(c -> c.accept(visitor)).toList();
                }
                return List.of();
            }
        }.visit(parser.program());
        return new PolicyProgram(statements);
    }
}
