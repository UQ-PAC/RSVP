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
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Pre-analysis policy transformations
 * - Convert expressions of the form 'e in [e1, e2]' to 'e in e1 || e in e2'
 * - Translate if-conditionals to boolean logic
 * - Translate set.containsAny/set.containsAll to disjunctions/conjunctions
 */
public class TranslationTransformer implements PolicyComputationVisitor<Expression> {

    private static final TranslationTransformer TRANSFORMER = new TranslationTransformer();

    private TranslationTransformer() {}

    public static Expression transform(Expression expr) {
        return expr.compute(TRANSFORMER);
    }

    Expression compute(Expression expr) {
        return expr.compute(this);
    }

    List<Expression> compute(List<Expression> expr) {
        return expr.stream().map(this::compute).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Expression visitBinaryExpr(BinaryExpression expr) {
        Expression lhs = expr.getLeft().compute(this);
        if (expr.getOp() == BinaryExpression.BinaryOp.In && expr.getRight() instanceof SetExpression set) {
            Expression init = new BooleanExpression(false);
            return set.getElements().stream()
                    .map(e ->  (Expression) new BinaryExpression(lhs, In, e))
                    .reduce(init, (l, r) -> new BinaryExpression(l, Or, r));
        } else {
            return new BinaryExpression(lhs, expr.getOp(), compute(expr.getRight()));
        }
    }

    @Override
    public Expression visitUnaryExpr(UnaryExpression expr) {
        return new UnaryExpression(expr.getOp(), compute(expr.getExpression()));
    }

    @Override
    public Expression visitCallExpr(CallExpression expr) {
        // Call expressions can be standalone
        Expression self = (expr.getSelf() == null) ? null : compute(expr.getSelf());
        List<Expression> args = compute(expr.getArgs());
        String fun = expr.getFunc();

        // set.containsAll([a, b]) -> set.contains(a) && set.contains(b)
        // set.containsAny([a, b]) -> set.contains(a) || set.contains(b)
        if (fun.equals("containsAll") || fun.contains("containsAny")) {
            require(expr.getArgs().size() == 1);
            BinaryExpression.BinaryOp op = fun.equals("containsAny") ? Or : And;
            if (expr.getArgs().getFirst() instanceof SetExpression set) {
                List<Expression> elements = new ArrayList<>(set.getElements());
                if (elements.isEmpty()) {
                    return new BooleanExpression(false);
                } else {
                    Expression init = new CallExpression(self, "contains", List.of(elements.removeFirst()));
                    return elements.stream()
                            .map(e -> (Expression) new CallExpression(self, "contains", List.of(e)))
                            .reduce(init, (l, r) -> new BinaryExpression(l, op, r));
                }
            }
        }
        // Here we only re-write the form where LHS is a literal set, another form, where the argument to
        // containsAll/containsAny is a property is handled by code generation
        return new CallExpression(self, expr.getFunc(), args);
    }

    @Override
    public Expression visitSetExpr(SetExpression expr) {
        return new SetExpression(expr.getElements().stream().map(this::compute).collect(Collectors.toSet()));
    }

    @Override
    public Expression visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return new PropertyAccessExpression(compute(expr.getObject()), expr.getProperty());
    }

    @Override
    public Expression visitConditionalExpr(ConditionalExpression expr) {
        // Transform expressions of the form
        ///  if x then y else z to
        // (x && y) || (!x && z)
        Expression cond = compute(expr.getCondition()),
                then = compute(expr.getThen()),
                els = compute(expr.getElse());

        Expression notCond = new UnaryExpression(Not, cond);
        return new BinaryExpression(new BinaryExpression(cond, And, then), Or,
                new BinaryExpression(notCond, And, els));
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
    public Expression visitSlotExpr(SlotExpression expr) {
        throw new TranslationError("Unsupported transformation for: " + expr);
    }
}
