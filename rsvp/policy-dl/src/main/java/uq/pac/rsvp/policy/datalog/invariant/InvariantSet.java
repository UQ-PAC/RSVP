package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class InvariantSet {
    private final Map<String, Invariant> invariants;

    private InvariantSet(Collection<Invariant> invariants) {
        Map<String, Invariant> in = new LinkedHashMap<>();
        for (Invariant i : invariants) {
            if (in.containsKey(i.getName())) {
                throw new TranslationError("Duplicate invariant name: " + i.getName());
            }
            in.put(i.getName(), i);
        }
        this.invariants = Collections.unmodifiableMap(in);
    }

    public Collection<Invariant> getInvariants() {
        return invariants.values();
    }

    public Stream<Invariant> stream() {
        return invariants.entrySet().stream().map(Map.Entry::getValue);
    }

    public Invariant getInvariant(String name) {
        return invariants.get(name);
    }

    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos, String msg, RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + pos + " " + msg);
        }
    }

    public static InvariantSet parse(Path file) throws IOException {
        return parse(Files.readString(file));
    }

    public static InvariantSet parse(String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        InvariantLexer lexer = new InvariantLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InvariantParser parser = new InvariantParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ExpressionVisitor sv = new ExpressionVisitor();
        List<Invariant> invariants = new InvariantBaseVisitor<List<Invariant>> () {
            @Override
            public List<Invariant> visitProgram(InvariantParser.ProgramContext ctx) {
                return ctx.invariant().stream().map(inv -> {
                    // Invariant expression
                    Expression expr = sv.visit(inv.expression());
                    // Quantifier is optional (defaults to ALL) unless variables are specified,
                    // since then this is basically a constant expression
                    Quantifier quantifier = null;
                    String str = inv.STRING().getText();
                    String name = str.substring(1, str.length() - 1);
                    if (inv.quantifier() != null) {
                        Quantifier.Scope scope = Quantifier.Scope.valueOf(inv.quantifier().quant.getText().toUpperCase());
                        Map<String, String> variables = new HashMap<>();
                        inv.quantifier().typedVariable().forEach(tv -> {
                            variables.put(tv.variable().getText(), ExpressionVisitor.getTypeExpression(tv.type()).getValue());
                        });
                        quantifier = new Quantifier(scope, variables);
                    }
                    return new Invariant(name, quantifier, expr);
                }).toList();
            }
        }.visit(parser.program());
        return new InvariantSet(invariants);
    }
}
