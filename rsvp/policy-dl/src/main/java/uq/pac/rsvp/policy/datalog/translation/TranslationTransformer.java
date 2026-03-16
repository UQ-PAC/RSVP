package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.*;
import java.util.stream.Collectors;


import static uq.pac.rsvp.policy.ast.expr.BinaryExpression.BinaryOp.*;
import static uq.pac.rsvp.policy.ast.expr.UnaryExpression.UnaryOp.*;
/**
 * Pre-analysis policy transformations
 * - Convert expressions of the form 'e in [e1, e2]' to 'e in e1 || e in e2'
 * - Translate if-conditionals to boolean logic
 */
public class TranslationTransformer implements PolicyComputationVisitor<Expression> {

    private static final TranslationTransformer TRANSFORMER = new TranslationTransformer();

    private TranslationTransformer() {}

    public static Expression transform(Expression expr) {
        return expr.compute(TRANSFORMER);
    }

    @Override
    public Expression visitBinaryExpr(BinaryExpression expr) {
        Expression lhs = expr.getLeft().compute(this);
        if (expr.getOp() == BinaryExpression.BinaryOp.In && expr.getRight() instanceof SetExpression set) {
            if (set.getElements().isEmpty()) {
                return new BooleanExpression(false);
            }
            Deque<Expression> deque = set.getElements().stream().map(e -> {
                return new BinaryExpression(lhs, BinaryExpression.BinaryOp.In, e);
            }).collect(Collectors.toCollection(LinkedList::new));
            Expression result = deque.removeFirst();
            while (!deque.isEmpty()) {
                result = new BinaryExpression(result, BinaryExpression.BinaryOp.Or, deque.removeFirst());
            }
            return result;
        } else {
            return new BinaryExpression(lhs, expr.getOp(), expr.getRight().compute(this));
        }
    }

    @Override
    public Expression visitUnaryExpr(UnaryExpression expr) {
        return expr;
    }

    // Atomic predicate expressions
    @Override
    public Expression visitCallExpr(CallExpression expr) {
        return expr;
    }

    @Override
    public Expression visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return expr;
    }

    @Override
    public Expression visitRecordExpr(RecordExpression expr) {
        return expr;
    }

    @Override
    public Expression visitVariableExpr(VariableExpression expr) {
        return expr;
    }

    @Override
    public Expression visitActionExpr(ActionExpression expr) {
        return expr;
    }

    @Override
    public Expression visitBooleanExpr(BooleanExpression expr) {
        return expr;
    }

    @Override
    public Expression visitEntityExpr(EntityExpression expr) {
        return expr;
    }

    @Override
    public Expression visitLongExpr(LongExpression expr) {
        return expr;
    }

    @Override
    public Expression visitStringExpr(StringExpression expr) {
        return expr;
    }

    @Override
    public Expression visitTypeExpr(TypeExpression expr) {
        return expr;
    }

    // Unsupported expressions
    @Override
    public Expression visitPolicySet(PolicySet set) {
        throw new TranslationError("Unsupported transformation for: " + set);
    }

    @Override
    public Expression visitPolicy(Policy policy) {
        throw new TranslationError("Unsupported transformation for: " + policy);
    }

    @Override
    public Expression visitConditionalExpr(ConditionalExpression expr) {
        // Transform expressions of the form
        ///  if x then y else z to
        // (x && y) || (!x && z)
        Expression cond = expr.getCondition().compute(this),
            then = expr.getThen().compute(this),
            els = expr.getElse().compute(this);

        Expression notCond = new UnaryExpression(Not, cond);
        return new BinaryExpression(new BinaryExpression(cond, And, then), Or,
                new BinaryExpression(notCond, And, els));
    }

    @Override
    public Expression visitSlotExpr(SlotExpression expr) {
        throw new TranslationError("Unsupported transformation for: " + expr);
    }

    @Override
    public Expression visitSetExpr(SetExpression expr) {
        throw new TranslationError("Unsupported transformation for: " + expr);
    }
}
