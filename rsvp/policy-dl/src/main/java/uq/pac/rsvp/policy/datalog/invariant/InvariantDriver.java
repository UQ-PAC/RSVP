package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.expr.Expression;

import java.util.List;
import java.util.Locale;

public class InvariantDriver {

   static String INPUT = """
        for all true;
        for all false;
        for all principal;
        for some principal.album.photo;
        for some Principal::Album::Photo;
        for some Resource::Picture::Kind::"Forest";
        for some principal.album.photo && resource;
        for some principal.album.photo || resource;
        for some (principal.album.photo || resource);
        for some !principal.album.photo;
        for some !(principal.album.photo || resource);
        for some a == b && c != d;
        for some resource has foo || principal has bar;
        for some resource is Resource::Picture::Kind;
        for some resource in Resource::Picture::Kind::"Forest";
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
            public List<Invariant> visitProgram(InvariantParser.ProgramContext ctx) {
                return ctx.invariant().stream().map(i -> {
                    Quantifier quantifier = Quantifier.valueOf(i.op.getText().toUpperCase(Locale.ROOT));
                    Expression expr = sv.visit(i.expression());
                    return new Invariant(quantifier, expr);
                }).toList();
            }
        }.visit(parser.program());
    }

    public static void main(String [] args) {
        parse(INPUT).forEach(System.out::println);
    }
}
