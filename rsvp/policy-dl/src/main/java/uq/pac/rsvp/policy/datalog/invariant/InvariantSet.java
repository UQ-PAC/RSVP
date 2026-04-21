package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.support.FileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class InvariantSet {
    private final Set<Invariant> invariants;

    private InvariantSet(Collection<Invariant> invariants) {
        this.invariants = Collections.unmodifiableSet(new LinkedHashSet<>(invariants));
    }

    public Collection<Invariant> getInvariants() {
        return invariants;
    }

    public Stream<Invariant> stream() {
        return invariants.stream();
    }

    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos, String msg, RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + pos + " " + msg);
        }
    }

    public static InvariantSet parse(Path file) throws IOException {
        return parse(file.toString(), Files.readString(file));
    }


    public static InvariantSet parse(String text) {
        return parse("unknown", text);
    }

    public static InvariantSet parse(String file, String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarLexer lexer = new CedarLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarParser parser = new CedarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        FileSource fs = new FileSource(file, text);
        ExpressionVisitor sv = new ExpressionVisitor(fs);
        List<Invariant> invariants = new SourceVisitor<List<Invariant>>(fs) {
            @Override
            public List<Invariant> visitProgram(CedarParser.ProgramContext ctx) {
                return ctx.invariant().stream().map(inv -> {
                    // Invariant expression
                    Expression expr = sv.visit(inv.expression());
                    // Quantifier is optional (defaults to ALL) unless variables are specified,
                    // since then this is basically a constant expression
                    InvariantQuantifier quantifier = null;
                    if (inv.quantifier() != null) {
                        InvariantQuantifier.Scope scope = InvariantQuantifier.Scope.valueOf(inv.quantifier().quant.getText().toUpperCase());
                        List<InvariantQuantifier.Variable> variables =
                                inv.quantifier().typedVariable().stream().map(tv ->
                                        new InvariantQuantifier.Variable(tv.variable().getText(),
                                                sv.getTypeExpression(tv.type()).getType()))
                                .toList();
                        quantifier = new InvariantQuantifier(scope, variables);
                    }
                    return new Invariant(quantifier, expr);
                }).toList();
            }
        }.visit(parser.program());
        return new InvariantSet(invariants);
    }
}
