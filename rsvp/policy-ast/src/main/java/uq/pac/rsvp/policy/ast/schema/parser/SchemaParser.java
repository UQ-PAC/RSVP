/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema.parser;

import org.antlr.v4.runtime.*;
import uq.pac.rsvp.policy.ast.CedarschemaLexer;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.ThrowingErrorListener;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.SchemaStatement;
import uq.pac.rsvp.support.FileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parsing cedar policies and invariants as a collection of statements
 */
public class SchemaParser {
    public static Schema parse(Path path) throws IOException {
        String file = path.getFileName().toString();
        String text = Files.readString(path);
        return parse(file, text);
    }

    public static Schema parse(String file, String text) {
        FileSource fs = new FileSource(file, text);
        ThrowingErrorListener errorListener = new ThrowingErrorListener(fs);

        CedarschemaLexer lexer = new CedarschemaLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarschemaParser parser = new CedarschemaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        List<SchemaStatement> components = new ArrayList<>();

        return new CedarschemaSourceVisitor<Schema>(fs) {
            @Override
            public Schema visitSchema(CedarschemaParser.SchemaContext ctx) {
                SchemaStatementVisitor statements = new SchemaStatementVisitor(fs, "");

                for (CedarschemaParser.StatementContext s : ctx.statement()) {
                    components.addAll(statements.visit(s));
                }

                for (CedarschemaParser.NamespaceContext ns : ctx.namespace()) {
                    String namespace  = ns.path().getText();
                    statements = new SchemaStatementVisitor(fs, namespace);
                    for (CedarschemaParser.StatementContext s : ns.statement()) {
                        components.addAll(statements.visit(s));
                    }
                }
                return Schema.of(components);
            }
        }.visit(parser.schema());
    }
}
