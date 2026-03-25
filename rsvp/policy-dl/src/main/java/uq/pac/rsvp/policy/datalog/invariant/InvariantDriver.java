package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.policy.ast.expr.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvariantDriver {

    static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos, String msg, RecognitionException e) {
            throw new ParseCancellationException("line " + line + ":" + pos + " " + msg);
        }
    }

    public static List<Invariant> parse(String text) {
        ThrowingErrorListener errorListener = new ThrowingErrorListener();

        InvariantLexer lexer = new InvariantLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InvariantParser parser = new InvariantParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ExpressionVisitor sv = new ExpressionVisitor();
        return new InvariantBaseVisitor<List<Invariant>> () {
            @Override
            public List<Invariant> visitProgram(InvariantParser.ProgramContext ctx) {
                return ctx.invariant().stream().map(inv -> {
                    // Invariant expression
                    Expression expr = sv.visit(inv.expression());
                    // Quantifier is optional (defaults to ALL) unless variables are specified,
                    // since then this is basically a constant expression
                    Quantifier quantifier = null;
                    String str = inv.STRING().getText();
                    String name = str.substring(1, str.length() - 1);
                    if (inv.quantifier() != null) {
                        Quantifier.Scope scope = Quantifier.Scope.valueOf(inv.quantifier().quant.getText().toUpperCase());
                        Map<String, String> variables = new HashMap<>();
                        inv.quantifier().typedVariable().forEach(tv -> {
                            variables.put(tv.variable().getText(), ExpressionVisitor.getTypeExpression(tv.type()).getValue());
                        });
                        quantifier = new Quantifier(scope, variables);
                    }
                    return new Invariant(name, quantifier, expr);
                }).toList();
            }
        }.visit(parser.program());
    }
}
