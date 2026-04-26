package uq.pac.rsvp.policy.ast.schema;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.*;
import uq.pac.rsvp.support.FileSource;

/**
 * Parsing cedar policies and invariants as a collection of statements
 */
public class SchemaParser {
    public static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                                String msg, RecognitionException e) {
            throw new ParseCancellationException("Parse error: " + line + ":" + pos + " " + msg);
        }
    }

    public static Schema parse(String file, String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarschemaLexer lexer = new CedarschemaLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarschemaParser parser = new CedarschemaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        FileSource source = new FileSource(file, text);

        SchemaTypeVisitor types = new SchemaTypeVisitor(source);

        Schema schema = new CedarschemaBaseVisitor<Schema>() {
            @Override
            public Schema visitSchema(CedarschemaParser.SchemaContext ctx) {
                Schema schema = new Schema();
                Namespace global = new Namespace("");
                SchemaStatementVisitor gs = new SchemaStatementVisitor(source, types, global);
                ctx.statement().forEach(stmt -> {
                    global.add(gs.visit(stmt));
                });
                schema.add(global);

                ctx.namespace().forEach(n -> {
                    Namespace ns = new Namespace(n.path().getText());
                    SchemaStatementVisitor ls = new SchemaStatementVisitor(source, types, ns);
                    n.statement().forEach(stmt -> {
                        global.add(ls.visit(ctx));
                        schema.add(ns);
                    });
                });
                return schema;
            }
        }.visit(parser.schema());
        SchemaResolutionVisitor resolver = new SchemaResolutionVisitor();
        schema.accept(resolver);
        return schema;
    }
}
