package uq.pac.rsvp.policy.ast.schema.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.CedarschemaLexer;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.SchemaStatement;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    public static Schema parse(Path path) throws IOException {
        String file = path.getFileName().toString();
        String text = Files.readString(path);
        return parse(file, text);
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
        List<SchemaStatement> components = new ArrayList<>();

        return new CedarschemaSourceVisitor<Schema>(source) {
            @Override
            public Schema visitSchema(CedarschemaParser.SchemaContext ctx) {
                SchemaStatementVisitor statements = new SchemaStatementVisitor(source, "");

                for (CedarschemaParser.StatementContext s : ctx.statement()) {
                    components.addAll(statements.visit(s));
                }

                for (CedarschemaParser.NamespaceContext ns : ctx.namespace()) {
                    String namespace  = ns.path().getText();
                    statements = new SchemaStatementVisitor(source, namespace);
                    for (CedarschemaParser.StatementContext s : ns.statement()) {
                        components.addAll(statements.visit(s));
                    }
                }
                SourceLoc location = components.isEmpty() ? SourceLoc.MISSING : location(ctx);
                return Schema.build(components, location);
            }
        }.visit(parser.schema());
    }
}
