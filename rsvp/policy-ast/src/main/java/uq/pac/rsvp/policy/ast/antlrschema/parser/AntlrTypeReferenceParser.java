package uq.pac.rsvp.policy.ast.antlrschema.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import uq.pac.rsvp.policy.ast.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parse text into a type reference
 */
public class AntlrTypeReferenceParser {
    public static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                                String msg, RecognitionException e) {
            throw new ParseCancellationException("Parse error: " + line + ":" + pos + " " + msg);
        }
    }

    public static AntlrTypeReference parse(String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        CedarTypeReferenceLexer lexer = new CedarTypeReferenceLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarTypeReferenceParser parser = new CedarTypeReferenceParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return new CedarTypeReferenceBaseVisitor<AntlrTypeReference>() {
            @Override
            public AntlrTypeReference visitReference(CedarTypeReferenceParser.ReferenceContext ctx) {
                List<String> path = ctx.path().ID().stream()
                        .map(ParseTree::getText)
                        .collect(Collectors.toCollection(ArrayList::new));

                // If there is no string, it means an entity type with the last
                // element being the name and the rest namespace
                String name = path.removeLast();
                String namespace ;
                if (ctx.STRING() != null) {
                    // If there is a string, it means that's an action
                    // And if so the last element of teh path should be literal 'Action',
                    // and otherwise the reference is malformed
                    if (!name.equals("Action")) {
                        throw new ParseCancellationException("Invalid reference: " + ctx.getText());
                    }
                    name = name + "::" + ctx.STRING().getText();

                }
                namespace = String.join("::", path);
                return new AntlrTypeReference(namespace, name);
            }
        }.visit(parser.reference());
    }
}
