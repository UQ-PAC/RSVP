package uq.pac.rsvp.policy.ast.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.*;
import uq.pac.rsvp.policy.ast.CedarLexer;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.CedarschemaBaseVisitor;
import uq.pac.rsvp.policy.ast.CedarschemaLexer;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.CedarschemaVisitor;
import uq.pac.rsvp.support.FileSource;

import java.util.Collection;
import java.util.List;

/**
 * Parsing cedar policies and invariants as a collection of statements
 */
public class SchemaParser {
    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                                String msg, RecognitionException e) {
            throw new ParseCancellationException("Parse error: " + line + ":" + pos + " " + msg);
        }
    }

    public static String parse(String file, String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarschemaLexer lexer = new CedarschemaLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarschemaParser parser = new CedarschemaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return new CedarschemaBaseVisitor<String>() {
            @Override
            public String visitSchema(CedarschemaParser.SchemaContext ctx) {
                return ctx.getText();
            }
        }.visit(parser.schema());
    }
}
