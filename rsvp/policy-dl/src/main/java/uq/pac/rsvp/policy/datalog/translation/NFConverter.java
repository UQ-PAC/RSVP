package uq.pac.rsvp.policy.datalog.translation;

import org.logicng.formulas.*;
import org.logicng.transformations.dnf.DNFFactorization;
import uq.pac.rsvp.policy.ast.expr.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converting a Cedar expression to a normal form
 */
public class NFConverter extends ValueVisitorAdapter<Formula> {
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

    public static List<List<Expression>> toDNF(Expression expr) {
        NFConverter converter = new NFConverter();
        Formula formula = expr.compute(converter);
        Formula nf = formula.transform(new DNFFactorization());
        Stream<Formula> disjunctions = nf instanceof Or or ? or.stream() : Stream.of(nf);

        return disjunctions.map(dis -> {
            return (dis instanceof And and ? and.stream() : Stream.of(dis)).toList();
        }).map(dis -> {
            return dis.stream()
                    .map(converter::toExpression)
                    .sorted(Comparator.comparingInt(ExpressionOrdering::order))
                    .toList();
        }).toList();
    }
}
