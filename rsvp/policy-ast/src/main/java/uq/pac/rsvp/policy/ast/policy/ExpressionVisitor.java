package uq.pac.rsvp.policy.ast.policy;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.support.FileSource;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.ast.expr.BinaryExpression.BinaryOp.*;

class ExpressionVisitor extends SourceVisitor<Expression> {

    public ExpressionVisitor(FileSource fs) {
        super(fs);
    }

    @Override
    public Expression visitType(CedarParser.TypeContext ctx) {
        String type = ctx.ID().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining("::"));
        return new TypeExpression(type, location(ctx));
    }

    @Override
    public Expression visitEntity(CedarParser.EntityContext ctx) {
        String type = ((TypeExpression) visitType(ctx.type())).getValue();
        String eid = ctx.STRING().getText();
        eid = eid.substring(1, eid.length() - 1);
        if (type.equals("Action") || type.endsWith("::Action")) {
            return new ActionExpression(eid, type, location(ctx));
        } else {
            return new EntityExpression(eid, type, location(ctx));
        }
    }

    @Override
    public Expression visitVariable(CedarParser.VariableContext ctx) {
        return new VariableExpression(ctx.getText(), location(ctx));
    }

    @Override
    public Expression visitVariableExpr(CedarParser.VariableExprContext ctx) {
        return visitVariable(ctx.variable());
    }

    @Override
    public Expression visitConditionalExpr(CedarParser.ConditionalExprContext ctx) {
        List<Expression> condition = ctx.expression().stream()
                .map(e -> e.accept(this))
                .toList();
        return new ConditionalExpression(condition.get(0), condition.get(1), condition.get(2), location(ctx));
    }

    @Override
    public Expression visitStringExpr(CedarParser.StringExprContext ctx) {
        String value = ctx.STRING().getText();
        return new StringExpression(StringExpression.unescape(value.substring(1, value.length() - 1)), location(ctx));
    }

    @Override
    public Expression visitLongExpr(CedarParser.LongExprContext ctx) {
        return new LongExpression(Long.parseLong(ctx.LONG().getText()), location(ctx));
    }

    @Override
    public Expression visitConjunctionExpr(CedarParser.ConjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.BinaryOp.And, ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitNegationExpr(CedarParser.NegationExprContext ctx) {
        return new UnaryExpression(UnaryExpression.UnaryOp.Not, ctx.expression().accept(this), location(ctx));
    }

    @Override
    public Expression visitArithNegationExpr(CedarParser.ArithNegationExprContext ctx) {
        return new UnaryExpression(UnaryExpression.UnaryOp.Neg, ctx.expression().accept(this), location(ctx));
    }

    @Override
    public Expression visitDisjunctionExpr(CedarParser.DisjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.BinaryOp.Or, ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitLiteralExpr(CedarParser.LiteralExprContext ctx) {
        return new BooleanExpression(Boolean.parseBoolean(ctx.getText()), location(ctx));
    }

    @Override
    public Expression visitGroupingExpr(CedarParser.GroupingExprContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitIsExpr(CedarParser.IsExprContext ctx) {
        return new BinaryExpression(ctx.expression().accept(this), Is, visitType(ctx.type()), location(ctx));
    }

    @Override
    public Expression visitInExpr(CedarParser.InExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.BinaryOp.In, ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitHasExpr(CedarParser.HasExprContext ctx) {
        String attr = ctx.attr.getText();
        if (attr.startsWith("\"")) {
            attr = attr.substring(1, attr.length() - 1);
        }
        return new BinaryExpression(ctx.expression().accept(this),
                HasAttr, new StringExpression(attr), location(ctx));
    }

    @Override
    public Expression visitComparisonExpr(CedarParser.ComparisonExprContext ctx) {
        BinaryExpression.BinaryOp op = switch (ctx.op.getText()) {
            case "==" -> Eq;
            case "!=" -> Neq;
            case ">" -> Greater;
            case "<" -> Less;
            case ">=" -> GreaterEq;
            case "<=" -> LessEq;
            default -> throw new AssertionError("unreachable");
        };
        return new BinaryExpression(ctx.expression(0).accept(this), op,
                ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitArithExpr(CedarParser.ArithExprContext ctx) {
        BinaryExpression.BinaryOp op = switch (ctx.op.getText()) {
            case "+" -> BinaryExpression.BinaryOp.Add;
            case "-" -> BinaryExpression.BinaryOp.Sub;
            case "*" -> BinaryExpression.BinaryOp.Mul;
            default -> throw new AssertionError("unreachable");
        };
        return new BinaryExpression(ctx.expression(0).accept(this), op,
                ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitCallExpr(CedarParser.CallExprContext ctx) {
        Expression object = null;
        if (ctx.property() != null) {
            List<String> properties = ctx.property().variable().stream()
                    .map(RuleContext::getText)
                    .collect(Collectors.toList());
            object = new VariableExpression(properties.removeFirst());
            while (!properties.isEmpty()) {
                String prop = properties.removeFirst();
                object = new PropertyAccessExpression(object, prop);
            }
        }
        String fun = ctx.ID().getText();
        List<Expression> args = Collections.emptyList();
        if (ctx.expressionList() != null) {
            args = ctx.expressionList().expression().stream()
                    .map(c -> c.accept(this))
                    .toList();
        }
        return new CallExpression(object, fun, args, location(ctx));
    }

    @Override
    public Expression visitTypeExpr(CedarParser.TypeExprContext ctx) {
        return visitType(ctx.type());
    }

    @Override
    public Expression visitEntityExpr(CedarParser.EntityExprContext ctx) {
        return visitEntity(ctx.entity());
    }

    @Override
    public Expression visitSetExpr(CedarParser.SetExprContext ctx) {
        Set<Expression> expressions = Collections.emptySet();
        if (ctx.expressionList() != null) {
            expressions = ctx.expressionList().expression().stream()
                    .map(e -> e.accept(this))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return new SetExpression(expressions, location(ctx));
    }

    @Override
    public Expression visitPropertyExpr(CedarParser.PropertyExprContext ctx) {
        List<String> properties = ctx.property().variable().stream()
                .map(RuleContext::getText)
                .collect(Collectors.toList());
        Expression object = new VariableExpression(properties.removeFirst());
        while (!properties.isEmpty()) {
            String prop = properties.removeFirst();
            object = new PropertyAccessExpression(object, prop);
        }
        return object;
    }

    @Override
    public Expression visitInvariant(CedarParser.InvariantContext ctx) {
        throw new AssertionError("Invariant in expression visitor");
    }

    @Override
    public Expression visitProgram(CedarParser.ProgramContext ctx) {
        throw new AssertionError("Program in expression visitor");
    }
}
