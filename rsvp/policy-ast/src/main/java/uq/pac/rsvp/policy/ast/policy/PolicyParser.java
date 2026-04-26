package uq.pac.rsvp.policy.ast.policy;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.CedarLexer;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.support.FileSource;

import java.util.Collection;
import java.util.List;

/**
 * Parsing cedar policies and invariants as a collection of statements
 */
public class PolicyParser {
    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                                String msg, RecognitionException e) {
            throw new ParseCancellationException("Parse error: " + line + ":" + pos + " " + msg);
        }
    }

    public static Collection<PolicyStatement> parse(String file, String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarLexer lexer = new CedarLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarParser parser = new CedarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        FileSource fs = new FileSource(file, text);
        PolicyStatementVisitor visitor = new PolicyStatementVisitor(fs);
        return new SourceVisitor<List<PolicyStatement>>(fs) {
            @Override
            public List<PolicyStatement> visitProgram(CedarParser.ProgramContext ctx) {
                if (ctx.children != null) {
                    return ctx.children.stream().map(c -> c.accept(visitor)).toList();
                }
                return List.of();
            }
        }.visit(parser.program());
    }
}
