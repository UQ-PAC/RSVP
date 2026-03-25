package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.List;
import java.util.stream.Collectors;

class ExpressionVisitor extends InvariantBaseVisitor<Expression> {

    public ExpressionVisitor() {}

    static TypeExpression getTypeExpression(InvariantParser.TypeContext ctx) {
        String type = ctx.ID().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining("::"));
        return new TypeExpression(type);
    }

    static EntityExpression getEntityExpression(InvariantParser.EntityContext ctx) {
        String type = getTypeExpression(ctx.type()).getValue();
        String eid = ctx.STRING().getText();
        return new EntityExpression(eid.substring(1, eid.length() - 1), type);
    }

    @Override
    public Expression visitVariableExpr(InvariantParser.VariableExprContext ctx) {
        return new VariableExpression(ctx.getText());
    }

    @Override
    public Expression visitStringExpr(InvariantParser.StringExprContext ctx) {
        String value = ctx.STRING().getText();
        return new StringExpression(value.substring(1, value.length() - 1));
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
    public Expression visitDisjunctionExpr(InvariantParser.DisjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.BinaryOp.Or, ctx.expression(1).accept(this));
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
                BinaryExpression.BinaryOp.In, getEntityExpression(ctx.entity()));
    }

    @Override
    public Expression visitHasExpr(InvariantParser.HasExprContext ctx) {
        return new BinaryExpression(ctx.expression().accept(this),
                BinaryExpression.BinaryOp.HasAttr, new StringExpression(ctx.ID().getText()));
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
        return getEntityExpression(ctx.entity());
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
