package uq.pac.rsvp.policy.ast.antlrschema.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.CedarschemaBaseVisitor;
import uq.pac.rsvp.policy.ast.CedarschemaLexer;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrSchemaStatement;
import uq.pac.rsvp.support.FileSource;

import java.lang.module.ResolutionException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Parsing cedar policies and invariants as a collection of statements
 */
public class AntlrSchemaParser {
    public static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                                String msg, RecognitionException e) {
            throw new ParseCancellationException("Parse error: " + line + ":" + pos + " " + msg);
        }
    }

    public static AntlrSchema parse(String file, String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarschemaLexer lexer = new CedarschemaLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarschemaParser parser = new CedarschemaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        FileSource source = new FileSource(file, text);
        Map<String, AntlrSchemaStatement> components = new HashMap<>();

        Consumer<AntlrSchemaStatement> add = stmt -> {
            AntlrSchemaStatement s = components.get(stmt.getBaseName());
            if (s != null) {
                throw new ResolutionException("Statement %s illegally shadows %s"
                        .formatted(stmt.getName(), s.getName()));
            }
            components.put(stmt.getName(), stmt);
        };

        return new CedarschemaBaseVisitor<AntlrSchema>() {
            @Override
            public AntlrSchema visitSchema(CedarschemaParser.SchemaContext ctx) {
                AntlrSchemaStatementVisitor statements = new AntlrSchemaStatementVisitor(source, "");

                for (CedarschemaParser.StatementContext s : ctx.statement()) {
                    add.accept(statements.visit(s));
                }

                for (CedarschemaParser.NamespaceContext ns : ctx.namespace()) {
                    String namespace  = ns.path().getText();
                    statements = new AntlrSchemaStatementVisitor(source, namespace);
                    for (CedarschemaParser.StatementContext s : ns.statement()) {
                        add.accept(statements.visit(s));
                    }
                }
                return new AntlrSchema(components);
            }
        }.visit(parser.schema());
    }
}
