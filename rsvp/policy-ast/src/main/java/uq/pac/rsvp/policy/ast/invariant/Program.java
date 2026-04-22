package uq.pac.rsvp.policy.ast.invariant;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.CedarLexer;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.Statement;
import uq.pac.rsvp.support.FileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Program {
    private final List<Statement> statements;

    private Program(Collection<Statement> statements) {
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

    public static Program parse(Path file) throws IOException {
        return parse(file.toString(), Files.readString(file));
    }

    public static Program parse(String text) {
        return parse("unknown", text);
    }

    public static Program parse(String file, String text) {
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
        return new Program(statements);
    }
}
