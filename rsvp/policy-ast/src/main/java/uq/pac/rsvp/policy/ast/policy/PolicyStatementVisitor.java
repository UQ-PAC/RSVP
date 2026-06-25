/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy;

import org.antlr.v4.runtime.tree.TerminalNode;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class PolicyStatementVisitor extends CedarSourceVisitor<PolicyStatement> {

    private final ExpressionVisitor expressions;
    private final PolicyNaming naming;

    private static class PolicyNaming {
        private int index = 0;
        private static final String BASENAME = "policy";

        public String getName(String annotation) {
            return annotation != null ? annotation : BASENAME + index++;
        }

        public String getName() {
            return getName(null);
        }
    }

    public PolicyStatementVisitor(FileSource fs) {
        super(fs);
        this.expressions = new ExpressionVisitor(fs);
        this.naming = new PolicyNaming();
    }

    @Override
    public PolicyStatement visitInvariant(CedarParser.InvariantContext ctx) {
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

    private static String unquote(String s) {
        if (s != null) {
            return s.substring(1, s.length() - 1);
        }
        return null;
    }

    /**
     * A policy is represented as a single expression built as a conjunction of syntactic elements.
     * The following source location is used to indicate that the location of the element cannot be
     * determined, for instance, if it is conjunction of the policy preamble. The location is
     * made package-visible for testing purposes
     */
    final static SourceLoc OMITTED = SourceLoc.empty();

    private Expression getVariableScopeExpression(TerminalNode term, CedarParser.VariableScopeContext ctx) {
        if (ctx == null) {
            return null;
        }

        VariableExpression var = new VariableExpression(term.getText(), location(term));
        SourceLoc loc = location(term.getSymbol(), ctx.stop);
        if (ctx.IN() != null && ctx.IS() != null) {
            Expression lhs = new BinaryExpression(var, In, expressions.visitEntity(ctx.entity()), OMITTED);
            Expression rhs = new IsExpression(var, (TypeExpression) expressions.visitType(ctx.type()), OMITTED);
            return new BinaryExpression(lhs, And, rhs, loc);
        } else if (ctx.EQ() != null) {
            return new BinaryExpression(var, Eq, expressions.visitEntity(ctx.entity()), loc);
        } else if (ctx.IN() != null) {
            return new BinaryExpression(var, In, expressions.visitEntity(ctx.entity()), loc);
        } else if (ctx.IS() != null) {
            return new IsExpression(var, (TypeExpression) expressions.visitType(ctx.type()), loc);
        } else {
            return null;
        }
    }

    private Expression getPrincipalExpression(CedarParser.PrincipalContext ctx) {
        return getVariableScopeExpression(ctx.PRINCIPAL(), ctx.variableScope());
    }

    private Expression getResourceExpression(CedarParser.ResourceContext ctx) {
        return getVariableScopeExpression(ctx.RESOURCE(), ctx.variableScope());
    }

    private Expression getActionExpression(CedarParser.ActionContext ctx) {
        VariableExpression var = new VariableExpression(ctx.ACTION().getText(), location(ctx.ACTION()));
        // form: action == entity
        if (ctx.EQ() != null) {
            return new BinaryExpression(var, Eq, expressions.visitEntity(ctx.entity(0)), location(ctx));
        }

        // form: action in [...] | entity
        if (ctx.IN() != null && !ctx.entity().isEmpty()) {
            Set<Expression> entities = ctx.entity().stream()
                    .map(expressions::visitEntity)
                    .collect(Collectors.toSet());
            Expression elements = entities.size() == 1 ?
                    // Single element
                    entities.stream().findAny().orElseThrow() :
                    // Multiple
                    new SetExpression(entities, location(
                            ctx.LBRACKET().getSymbol(),
                            ctx.RBRACKET().getSymbol()));
            // Set location on the entire expression
            return new BinaryExpression(var, In, elements, location(ctx));
        }
        return null;
    }

    @Override
    public PolicyStatement visitPolicy(CedarParser.PolicyContext ctx) {
        Policy.Builder builder = new Policy.Builder();

        if (ctx.annotation() != null) {
            ctx.annotation().forEach(a -> {
                String value = "";
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
                .and(getPrincipalExpression(ctx.principal()), OMITTED)
                .and(getResourceExpression(ctx.resource()), OMITTED)
                .and(getActionExpression(ctx.action()), OMITTED)
                .effect(effect)
                .location(location(ctx));

        ctx.condition().forEach(cond -> {
            Expression e = cond.expression().accept(expressions);
            if (cond.WHEN() != null) {
                require(cond.UNLESS() == null);
                builder.and(e, OMITTED);
            } else if (cond.UNLESS() != null) {
                builder.andNot(e, OMITTED);
            } else {
                throw new AssertionError("Unknown condition");
            }
        });

        // Unless specified, the policy condition it true
        // Set it here so the omitted location can be used
        if (builder.condition() == null) {
            builder.and(new BooleanExpression(true, OMITTED), OMITTED);
        }

        return builder.build();
    }
}
