package uq.pac.rsvp.policy.datalog.translation;

import org.logicng.formulas.*;
import org.logicng.transformations.dnf.DNFFactorization;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converting a Cedar expression to a normal form
 */
public class NFConverter implements PolicyComputationVisitor<Formula> {
    private final Map<String, Expression> cache;
    private final FormulaFactory factory;
    private int counter = 0;

    private NFConverter() {
        this.cache = new HashMap<>();
        this.factory = new FormulaFactory();
    }

    private Variable getVar(Expression expr) {
        String name ="var" + counter++;
        cache.put(name, expr);
        return factory.variable(name);
    }

    @Override
    public Formula visitBinaryExpr(BinaryExpression expr) {
        return switch (expr.getOp()) {
            case BinaryExpression.BinaryOp.And ->
                factory.and(expr.getLeft().compute(this),
                        expr.getRight().compute(this));
            case BinaryExpression.BinaryOp.Or ->
                factory.or(expr.getLeft().compute(this),
                        expr.getRight().compute(this));
            default -> getVar(expr);
        };
    }

    @Override
    public Formula visitUnaryExpr(UnaryExpression expr) {
        return switch (expr.getOp()) {
            case UnaryExpression.UnaryOp.Not ->
                factory.not(expr.getExpression().compute(this));
            default -> getVar(expr);
        };
    }

    private Expression fromStream(Stream<Formula> stream, BinaryExpression.BinaryOp op) {
        List<Expression> exprs = stream
                .map(this::toExpression)
                .collect(Collectors.toCollection(LinkedList::new));
        Expression expr = exprs.removeFirst();
        while (!exprs.isEmpty()) {
            Expression next = exprs.removeFirst();
            expr = new BinaryExpression(expr, op, next);
        }
        return expr;
    }

    private Expression toExpression(Formula formula) {
        return switch (formula) {
            case Or or -> fromStream(or.stream(), BinaryExpression.BinaryOp.Or);
            case And and -> fromStream(and.stream(), BinaryExpression.BinaryOp.And);
            case Variable v -> cache.get(v.name());
            case Not n -> new UnaryExpression(UnaryExpression.UnaryOp.Not, toExpression(n.operand()));
            default -> throw new RuntimeException("Unreachable");
        };
    }

    public static Expression transform(Expression expr, FormulaTransformation transform) {
        NFConverter e2f = new NFConverter();
        Formula formula = expr.compute(e2f);
        Formula dnf = formula.transform(transform);
        return e2f.toExpression(dnf);
    }

    public static Expression toDNF(Expression expr) {
        return transform(expr, new DNFFactorization());
    }

    @Override
    public Formula visitPolicySet(PolicySet policySet) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitPolicy(Policy policy) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitPropertyAccessExpr(PropertyAccessExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitCallExpr(CallExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitConditionalExpr(ConditionalExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitRecordExpr(RecordExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitSetExpr(SetExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitVariableExpr(VariableExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitBooleanExpr(BooleanExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitEntityExpr(EntityExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitLongExpr(LongExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitSlotExpr(SlotExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitStringExpr(StringExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public Formula visitTypeExpr(TypeExpression expr) {
        throw new RuntimeException("unsupported");
    }
}
