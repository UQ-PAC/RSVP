package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

class InvariantExpressionVisitor extends InvariantBaseVisitor<Expression> {

    public InvariantExpressionVisitor() {}

    static TypeExpression getTypeExpression(InvariantParser.TypeContext ctx) {
        String type = ctx.ID().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining("::"));
        return new TypeExpression(type);
    }

    static Expression getActionOrEntityExpression(InvariantParser.EntityContext ctx) {
        String type = getTypeExpression(ctx.type()).getValue();
        String eid = ctx.STRING().getText();
        BiFunction<String, String, Expression> supplier = type.equals("Action") || type.endsWith("::Action") ?
                ActionExpression::new : EntityExpression::new;
        return supplier.apply(eid.substring(1, eid.length() - 1), type);
    }

    @Override
    public Expression visitVariableExpr(InvariantParser.VariableExprContext ctx) {
        return new VariableExpression(ctx.getText());
    }

    @Override
    public Expression visitConditionalExpr(InvariantParser.ConditionalExprContext ctx) {
        List<Expression> condition = ctx.expression().stream()
                .map(e -> e.accept(this))
                .toList();
        return new ConditionalExpression(condition.get(0), condition.get(1), condition.get(2));
    }

    @Override
    public Expression visitStringExpr(InvariantParser.StringExprContext ctx) {
        String value = ctx.STRING().getText();
        return new StringExpression(StringExpression.unescape(value.substring(1, value.length() - 1)));
    }

    @Override
    public Expression visitLongExpr(InvariantParser.LongExprContext ctx) {
        return new LongExpression(Long.parseLong(ctx.LONG().getText()));
    }

    @Override
    public Expression visitConjunctionExpr(InvariantParser.ConjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.BinaryOp.And, ctx.expression(1).accept(this));
    }

    @Override
    public Expression visitNegationExpr(InvariantParser.NegationExprContext ctx) {
        return new UnaryExpression(UnaryExpression.UnaryOp.Not, ctx.expression().accept(this));
    }

    @Override
    public Expression visitArithNegationExpr(InvariantParser.ArithNegationExprContext ctx) {
        return new UnaryExpression(UnaryExpression.UnaryOp.Neg, ctx.expression().accept(this));
    }

    @Override
    public Expression visitDisjunctionExpr(InvariantParser.DisjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.BinaryOp.Or, ctx.expression(1).accept(this));
    }

    @Override
    public Expression visitImplicationExpr(InvariantParser.ImplicationExprContext ctx) {
        Expression left = new UnaryExpression(UnaryExpression.UnaryOp.Not, ctx.expression(0).accept(this));
        return new BinaryExpression(left, BinaryExpression.BinaryOp.Or, ctx.expression(1).accept(this));
    }

    @Override
    public Expression visitLiteralExpr(InvariantParser.LiteralExprContext ctx) {
        return new BooleanExpression(Boolean.parseBoolean(ctx.getText()));
    }

    @Override
    public Expression visitGroupingExpr(InvariantParser.GroupingExprContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitIsExpr(InvariantParser.IsExprContext ctx) {
        return new BinaryExpression(ctx.expression().accept(this),
                BinaryExpression.BinaryOp.Is, getTypeExpression(ctx.type()));
    }

    @Override
    public Expression visitInExpr(InvariantParser.InExprContext ctx) {
        return new BinaryExpression(ctx.expression().accept(this),
                BinaryExpression.BinaryOp.In, getActionOrEntityExpression(ctx.entity()));
    }

    @Override
    public Expression visitHasExpr(InvariantParser.HasExprContext ctx) {
        String attr = ctx.attr.getText();
        if (attr.startsWith("\"")) {
            attr = attr.substring(1, attr.length() - 1);
        }
        return new BinaryExpression(ctx.expression().accept(this),
                BinaryExpression.BinaryOp.HasAttr, new StringExpression(attr));
    }

    @Override
    public Expression visitComparisonExpr(InvariantParser.ComparisonExprContext ctx) {
        BinaryExpression.BinaryOp op = switch (ctx.op.getText()) {
            case "==" -> BinaryExpression.BinaryOp.Eq;
            case "!=" -> BinaryExpression.BinaryOp.Neq;
            case ">" -> BinaryExpression.BinaryOp.Greater;
            case "<" -> BinaryExpression.BinaryOp.Less;
            case ">=" -> BinaryExpression.BinaryOp.GreaterEq;
            case "<=" -> BinaryExpression.BinaryOp.LessEq;
            default -> throw new AssertionError("unreachable");
        };
        return new BinaryExpression(ctx.expression(0).accept(this), op,
                ctx.expression(1).accept(this));
    }

    @Override
    public Expression visitArithExpr(InvariantParser.ArithExprContext ctx) {
        BinaryExpression.BinaryOp op = switch (ctx.op.getText()) {
            case "+" -> BinaryExpression.BinaryOp.Add;
            case "-" -> BinaryExpression.BinaryOp.Sub;
            case "*" -> BinaryExpression.BinaryOp.Mul;
            default -> throw new AssertionError("unreachable");
        };
        return new BinaryExpression(ctx.expression(0).accept(this), op,
                ctx.expression(1).accept(this));
    }

    @Override
    public Expression visitCallExpr(InvariantParser.CallExprContext ctx) {
        Expression object = null;
        if (ctx.property() != null) {
            List<TerminalNode> nodes = ctx.property().ID();
            object = new VariableExpression(nodes.removeFirst().getText());
            while (!nodes.isEmpty()) {
                String prop = nodes.removeFirst().getText();
                object = new PropertyAccessExpression(object, prop);
            }
        }
        String fun = ctx.ID().getText();
        List<Expression> args = ctx.callArguments().expression().stream()
                .map(c -> c.accept(this))
                .toList();

        return new CallExpression(object, fun, args);
    }

    @Override
    public Expression visitTypeExpr(InvariantParser.TypeExprContext ctx) {
        return getTypeExpression(ctx.type());
    }

    @Override
    public Expression visitEntityExpr(InvariantParser.EntityExprContext ctx) {
        // FIXME: Entity expression via Type expression, not string
        String type = ctx.entity().type().getText();
        return getActionOrEntityExpression(ctx.entity());
    }

    @Override
    public Expression visitPropertyExpr(InvariantParser.PropertyExprContext ctx) {
        List<TerminalNode> nodes = ctx.property().ID();
        Expression object = new VariableExpression(nodes.removeFirst().getText());
        while (!nodes.isEmpty()) {
            String prop = nodes.removeFirst().getText();
            object = new PropertyAccessExpression(object, prop);
        }
        return object;
    }

    @Override
    public Expression visitInvariant(InvariantParser.InvariantContext ctx) {
        throw new TranslationError("Invariant in expression visitor");
    }

    @Override
    public Expression visitProgram(InvariantParser.ProgramContext ctx) {
        throw new TranslationError("Program in expression visitor");
    }
}
