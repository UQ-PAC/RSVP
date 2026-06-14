package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A computation adapter re-combining expressions. In unmodified form clones an expression
 */
public class ExpressionAdapter implements PolicyComputationVisitor<Expression> {
    public Expression visitBinaryExpr(BinaryExpression expr) {
        Expression lhs = expr.getLeft().compute(this);
        Expression rhs = expr.getRight().compute(this);
        return new BinaryExpression(lhs, expr.getOperator(), rhs, expr.getSourceLoc());
    }

    public Expression visitCallExpr(CallExpression expr) {
        Expression self = expr.getSelf();
        if (self != null) {
            expr.getSelf().compute(this);
        }
        List<Expression> args = expr.getArgs().stream()
                .map(e -> e.compute(this))
                .toList();
        return new CallExpression(self, expr.getFunc(), args, expr.getSourceLoc());
    }

    public Expression visitConditionalExpr(ConditionalExpression expr) {
        Expression cond = expr.getCondition().compute(this),
                then = expr.getThen().compute(this),
                els = expr.getElse().compute(this);
        return new ConditionalExpression(cond, then, els, expr.getSourceLoc());
    }

    public Expression visitPropertyAccessExpr(PropertyAccessExpression expr) {
        Expression object = expr.getObject().compute(this);
        return new PropertyAccessExpression(object, expr.getProperty(), expr.getSourceLoc());
    }

    public Expression visitRecordExpr(RecordExpression expr) {
        Map<String, Expression> record = expr.getProperties().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().compute(this)));
        return new RecordExpression(record, expr.getSourceLoc());
    }

    public Expression visitSetExpr(SetExpression expr) {
        Set<Expression> expressions = expr.getElements().stream()
                .map(e -> e.compute(this))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new SetExpression(expressions, expr.getSourceLoc());
    }

    public Expression visitUnaryExpr(UnaryExpression expr) {
        return new UnaryExpression(expr.getOperator(),
                expr.getExpression().compute(this),
                expr.getSourceLoc());
    }

    public Expression visitVariableExpr(VariableExpression expr) {
        return expr;
    }

    public Expression visitActionExpr(ActionExpression expr) {
        return expr;
    }

    public Expression visitBooleanExpr(BooleanExpression expr) {
        return expr;
    }

    public Expression visitEntityExpr(EntityExpression expr) {
        return expr;
    }

    public Expression visitLongExpr(LongExpression expr) {
        return expr;
    }

    public Expression visitStringExpr(StringExpression expr) {
        return expr;
    }

    public Expression visitTypeExpr(TypeExpression expr) {
        return expr;
    }

    public Expression visitHasExpr(HasExpression expr) {
        Expression lhs = expr.getExpression().compute(this);
        return new HasExpression(lhs, expr.getAttribute(), expr.getSourceLoc());
    }

    public Expression visitIsExpr(IsExpression expr) {
        Expression lhs = expr.getExpression().compute(this);
        return new IsExpression(lhs, expr.getTypeExpression(), expr.getSourceLoc());
    }
}
