/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy;

import org.antlr.v4.runtime.*;
import uq.pac.rsvp.policy.ast.CedarLexer;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.ThrowingErrorListener;
import uq.pac.rsvp.policy.ast.policy.expr.Expression;
import uq.pac.rsvp.support.FileSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parsing cedar policies and invariants as a collection of statements
 */
public class PolicyParser {

    private record Parser (CedarParser parser, FileSource fs) { }

    private static Parser getParser(String filename, String text) {
        FileSource fs = new FileSource(filename, text);
        ThrowingErrorListener errorListener = new ThrowingErrorListener(fs);

        CedarLexer lexer = new CedarLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarParser parser = new CedarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return new Parser(parser, fs);
    }

    public static Expression parse(String text) {
        Parser parser = getParser("unknown", text);
        return new ExpressionVisitor(parser.fs).visit(parser.parser.expression());
    }

    public static Collection<PolicyStatement> parse(String file, String text) {
        Parser parser = getParser(file, text);
        FileSource fs = parser.fs;
        PolicyStatementVisitor visitor = new PolicyStatementVisitor(fs);
        return new CedarSourceVisitor<List<PolicyStatement>>(fs) {
            @Override
            public List<PolicyStatement> visitProgram(CedarParser.ProgramContext ctx) {
                List<PolicyStatement> statements = new ArrayList<>();
                for (int i = 0; i < ctx.children.size() - 1; i++) {
                    statements.add(ctx.children.get(i).accept(visitor));
                }
                return statements;
            }
        }.visit(parser.parser.program());
    }
}
