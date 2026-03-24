package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.expr.Expression;

import java.util.List;
import java.util.Locale;

public class InvariantDriver {

   static String INPUT = """
        for all principal.foo && resource.bar;
    """;

    public static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos, String msg, RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + pos + " " + msg);
        }
    }

    public static List<Invariant> parse(String program) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        InvariantLexer lexer = new InvariantLexer(CharStreams.fromString(program));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InvariantParser parser = new InvariantParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        InvariantExpressionVisitor sv = new InvariantExpressionVisitor();
        return new InvariantBaseVisitor<List<Invariant>> () {
            @Override
            public List<Invariant> visitInvariant(InvariantParser.InvariantContext ctx) {
                Quantifier quantifier = Quantifier.valueOf(ctx.op.getText().toUpperCase(Locale.ROOT));
                Expression expr = sv.visit(ctx.expression());
                Invariant in = new Invariant(quantifier, expr);
                return List.of();
            }
        }.visit(parser.program());
    }

    public static void main(String [] args) {
        parse(INPUT).forEach(System.out::println);
    }

}
