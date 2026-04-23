package uq.pac.rsvp.policy.ast.invariant;

import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.Statement;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.support.FileSource;

import static uq.pac.rsvp.policy.ast.expr.BinaryExpression.BinaryOp.*;

import java.util.ArrayList;
import java.util.List;

class StatementVisitor extends SourceVisitor<Statement> {

    private final ExpressionVisitor expressions;
    private final PolicyNaming naming;

    private static class PolicyNaming {
        private int index = 0;
        private static final String basename = "policy";

        public String getName(String annotation) {
            return annotation != null ? annotation : basename + index++;
        }

        public String getName() {
            return getName(null);
        }
    }

    public StatementVisitor(FileSource fs) {
        super(fs);
        this.expressions = new ExpressionVisitor(fs);
        this.naming = new PolicyNaming();
    }

    @Override
    public Statement visitInvariant(CedarParser.InvariantContext ctx) {
        // Invariant expression
        Expression expr = expressions.visit(ctx.expression());
        // Quantifier is optional (defaults to ALL) unless variables are specified,
        // since then this is basically a constant expression
        Quantifier quantifier = null;
        if (ctx.quantifier() != null) {
            Quantifier.Scope scope = Quantifier.Scope.valueOf(ctx.quantifier().quant.getText().toUpperCase());
            List<Quantifier.Variable> variables =
                    ctx.quantifier().typedVariable().stream().map(tv -> {
                        VariableExpression var = (VariableExpression) expressions.visitVariable(tv.variable());
                        TypeExpression type = (TypeExpression) expressions.visitType(tv.type());
                        return new Quantifier.Variable(var, type);
                    }).toList();
            quantifier = new Quantifier(scope, variables);
        }
        return new Invariant(quantifier, expr, location(ctx));
    }

    private String unquote(String s) {
        if (s != null) {
            return s.substring(1, s.length() - 1);
        }
        return null;
    }

    private Expression getPrincipalExpression(CedarParser.PrincipalContext ctx) {
        List<Expression> exprs = new ArrayList<>(2);
        VariableExpression var = new VariableExpression(ctx.PRINCIPAL().getText());
        if (ctx.EQ() != null) {
            exprs.add(new BinaryExpression(var, Eq, expressions.visitEntity(ctx.entity())));
        }
        if (ctx.IN() != null) {
            exprs.add(new BinaryExpression(var, In, expressions.visitEntity(ctx.entity())));
        }
        if (ctx.IS() != null) {
            exprs.add(new BinaryExpression(var, Is, expressions.visitType(ctx.type())));
        }

        return switch (exprs.size()) {
            case 0 -> null;
            case 1 -> exprs.getFirst();
            case 2 -> new BinaryExpression(exprs.getFirst(), And, exprs.getLast());
            default -> throw new AssertionError("unreachable");
        };
    }

    private Expression getResourceExpression(CedarParser.ResourceContext ctx) {
        List<Expression> exprs = new ArrayList<>(2);
        VariableExpression var = new VariableExpression(ctx.RESOURCE().getText());
        if (ctx.EQ() != null) {
            exprs.add(new BinaryExpression(var, Eq, expressions.visitEntity(ctx.entity())));
        }
        if (ctx.IN() != null) {
            exprs.add(new BinaryExpression(var, In, expressions.visitEntity(ctx.entity())));
        }
        if (ctx.IS() != null) {
            exprs.add(new BinaryExpression(var, Is, expressions.visitType(ctx.type())));
        }

        return switch (exprs.size()) {
            case 0 -> null;
            case 1 -> exprs.getFirst();
            case 2 -> new BinaryExpression(exprs.getFirst(), And, exprs.getLast());
            default -> throw new AssertionError("unreachable");
        };
    }

    private Expression getActionExpression(CedarParser.ActionContext ctx) {
        VariableExpression var = new VariableExpression(ctx.ACTION().getText());
        if (ctx.EQ() != null) {
            return new BinaryExpression(var, Eq, expressions.visitEntity(ctx.entity(0)));
        }
        return ctx.entity().stream()
                .map(e -> new BinaryExpression(var, In, expressions.visitEntity(e)))
                .reduce((a, b) -> new BinaryExpression(a, Or, b))
                .orElse(null);
    }

    @Override
    public Statement visitPolicy(CedarParser.PolicyContext ctx) {
        Policy.Builder builder = new Policy.Builder();

        if (ctx.annotation() != null) {
            ctx.annotation().forEach(a -> {
                String value = null;
                if (a.STRING() != null) {
                    value = unquote(a.STRING().getText());
                }
                builder.annotation(a.ID().getText(), value);
            });
        }

        Policy.Effect effect = switch (ctx.perm.getText()) {
            case "permit" -> Policy.Effect.Permit;
            case "forbid" -> Policy.Effect.Forbid;
            default -> throw new AssertionError("Unexpected value: " + ctx.perm.getText());
        };

        builder.name(naming.getName())
                .and(getPrincipalExpression(ctx.principal()))
                .and(getResourceExpression(ctx.resource()))
                .and(getActionExpression(ctx.action()))
                .effect(effect)
                .location(location(ctx));

        if (ctx.when() != null) {
            builder.and(ctx.when().expression().accept(expressions));
        }

        if (ctx.unless() != null) {
            builder.andNot(ctx.unless().expression().accept(expressions));
        }

        return builder.build();
    }
}
