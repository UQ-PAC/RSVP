package uq.pac.rsvp.policy.ast.policy;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import uq.pac.rsvp.policy.ast.CedarParser;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.support.FileSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.ast.Util.unquote;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.*;
import static uq.pac.rsvp.policy.ast.schema.type.TypeReference.TYPE_REFERENCE_DELIMITER;

public class ExpressionVisitor extends CedarSourceVisitor<Expression> {

    public ExpressionVisitor(FileSource fs) {
        super(fs);
    }

    @Override
    public Expression visitType(CedarParser.TypeContext ctx) {
        String type = ctx.ID().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining(TYPE_REFERENCE_DELIMITER));
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
        return new StringExpression(unquote(value), location(ctx));
    }

    @Override
    public Expression visitLongExpr(CedarParser.LongExprContext ctx) {
        return new LongExpression(Long.parseLong(ctx.LONG().getText()), location(ctx));
    }

    @Override
    public Expression visitConjunctionExpr(CedarParser.ConjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.Operator.And, ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitNegationExpr(CedarParser.NegationExprContext ctx) {
        return new UnaryExpression(UnaryExpression.Operator.Not, ctx.expression().accept(this), location(ctx));
    }

    @Override
    public Expression visitArithNegationExpr(CedarParser.ArithNegationExprContext ctx) {
        return new UnaryExpression(UnaryExpression.Operator.Neg, ctx.expression().accept(this), location(ctx));
    }

    @Override
    public Expression visitDisjunctionExpr(CedarParser.DisjunctionExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.Operator.Or, ctx.expression(1).accept(this), location(ctx));
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
        TypeExpression type = (TypeExpression) ctx.type().accept(this);
        return new IsExpression(ctx.expression().accept(this), type, location(ctx));
    }

    @Override
    public Expression visitInExpr(CedarParser.InExprContext ctx) {
        return new BinaryExpression(ctx.expression(0).accept(this),
                BinaryExpression.Operator.In, ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitHasExpr(CedarParser.HasExprContext ctx) {
        String attr = ctx.attributeName().getText();
        if (ctx.attributeName().STRING() != null) {
            attr = unquote(ctx.attributeName().STRING().getText());
        }
        Expression expr = ctx.expression().accept(this);
        return new HasExpression(expr, attr, location(ctx));
    }

    @Override
    public Expression visitComparisonExpr(CedarParser.ComparisonExprContext ctx) {
        BinaryExpression.Operator op = switch (ctx.op.getText()) {
            case "==" -> Eq;
            case "!=" -> Neq;
            case ">" -> Greater;
            case "<" -> Less;
            case ">=" -> GreaterEq;
            case "<=" -> LessEq;
            case "like" -> Like;
            default -> throw new AssertionError("unreachable");
        };
        return new BinaryExpression(ctx.expression(0).accept(this), op,
                ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitArithExpr(CedarParser.ArithExprContext ctx) {
        BinaryExpression.Operator op = switch (ctx.op.getText()) {
            case "+" -> BinaryExpression.Operator.Add;
            case "-" -> BinaryExpression.Operator.Sub;
            case "*" -> BinaryExpression.Operator.Mul;
            default -> throw new AssertionError("unreachable");
        };
        return new BinaryExpression(ctx.expression(0).accept(this), op,
                ctx.expression(1).accept(this), location(ctx));
    }

    @Override
    public Expression visitCallExpr(CedarParser.CallExprContext ctx) {
        Expression object = ctx.property() != null ? visitProperty(ctx.property()) : null;
        String fun = ctx.functionName().getText();
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
    public Expression visitRecordExpr(CedarParser.RecordExprContext ctx) {
        Map<String, Expression> attributes = new HashMap<>();
        if (ctx.attributes() != null) {
            for (CedarParser.AttributeContext ac : ctx.attributes().attribute()) {
                String name = ac.attributeName().getText();
                if (ac.attributeName().STRING() != null) {
                    name = unquote(name);
                }
                Expression expr = ac.expression().accept(this);
                attributes.put(name, expr);
            }
        }
        return new RecordExpression(attributes, location(ctx));
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
    public Expression visitProperty(CedarParser.PropertyContext ctx) {
        List<ParserRuleContext> properties = new ArrayList<>(ctx.variable());
        ParserRuleContext var = properties.removeFirst();
        Expression object = new VariableExpression(var.getText(), location(var));
        while (!properties.isEmpty()) {
            ParserRuleContext prop = properties.removeFirst();
            object = new PropertyAccessExpression(object, prop.getText(), location(var, prop));
        }
        return object;
    }

    @Override
    public Expression visitPropertyExpr(CedarParser.PropertyExprContext ctx) {
        return ctx.property().accept(this);
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
