package uq.pac.rsvp.policy.datalog.translation;

import org.logicng.formulas.*;
import org.logicng.transformations.dnf.DNFFactorization;
import org.logicng.transformations.NNFTransformation;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.And;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.Or;
import static uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression.Operator.Not;

/**
 * Converting a Cedar expression to DNF.
 * <p>
 * This conversion assumes that cedar expressions consist of conjunctions,
 * disjunctions and negations of  boolean-valued predicate expressions.
 * <p>
 * Conversion generates a list of lists of expressions, where each expression
 * list is a conjunctive clause. Each expression, in turn, is a potentially
 * negated predicate expression
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
            case And ->
                factory.and(expr.getLeft().compute(this), expr.getRight().compute(this));
            case Or ->
                factory.or(expr.getLeft().compute(this), expr.getRight().compute(this));
            // In Cedar boolean-valued expressions can be compared via, '==' or '!=', that is, it is possible
            // to construct something like '(a && b) == (c || d)'. In this case the expression is re-written to
            // '((a && b) && (c || d)) || (!(a && b) && !(c || d))'
            case Eq -> {
                if (isScalar(expr.getLeft()) && isScalar(expr.getRight())) {
                    yield getVar(expr);
                } else {
                    BinaryExpression lhs = new BinaryExpression(expr.getLeft(), And, expr.getRight());
                    Expression e1 = new UnaryExpression(Not, expr.getLeft());
                    Expression e2 = new UnaryExpression(Not, expr.getRight());
                    Expression rhs = new BinaryExpression(e1, And, e2);
                    yield new BinaryExpression(lhs, Or, rhs).compute(this);
                }
            }
            // != operator by this point should have been re-written to ==
            case Neq -> {
                throw new AssertionError("unreachable");
            }
            default -> getVar(expr);
        };
    }

    @Override
    public Formula visitCallExpr(CallExpression expr) {
        return getVar(expr);
    }

    @Override
    public Formula visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return getVar(expr);
    }

    @Override
    public Formula visitUnaryExpr(UnaryExpression expr) {
        return switch (expr.getOp()) {
            case UnaryExpression.Operator.Not -> factory.not(expr.getExpression().compute(this));
            case UnaryExpression.Operator.Neg -> getVar(expr);
        };
    }

    @Override
    public Formula visitHasExpr(HasExpression expr) {
        return getVar(expr);
    }

    @Override
    public Formula visitVariableExpr(VariableExpression expr) {
        return getVar(expr);
    }

    @Override
    public Formula visitIsExpr(IsExpression expr) {
        return getVar(expr);
    }

    @Override
    public Formula visitBooleanExpr(BooleanExpression expr) {
        return factory.constant(expr.getValue());
    }

    private Expression fromStream(Stream<Formula> stream, BinaryExpression.Operator op) {
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
            case Or or -> fromStream(or.stream(), Or);
            case And and -> fromStream(and.stream(), And);
            case Variable v -> cache.get(v.name());
			// Negated literal (equivalent of not)
            case Literal l -> l.phase() ? cache.get(l.variable().name()) :
                    new UnaryExpression(Not, toExpression(l.variable()));
            case Not n -> new UnaryExpression(Not, toExpression(n.operand()));
            case CTrue t -> new BooleanExpression(true);
            case CFalse f -> new BooleanExpression(false);
            default -> throw new RuntimeException("Unreachable");
        };
    }

    public static Expression toNNF(Expression expr) {
        NFConverter converter = new NFConverter();
        Formula formula = expr.compute(converter);
        Formula nf = formula.transform(NNFTransformation.get());
        return converter.toExpression(nf);
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
                    .toList();
        }).toList();
    }

    /**
     * Check if a given expression is scalar, i.e., has no logical connections
     * such as disjunction, conjunction or logical negation
     */
    public static boolean isScalar(Expression expr) {
        require(expr != null);

        if (expr instanceof UnaryExpression e && e.getOp() == Not) {
            expr = e.getExpression();
        }

        return expr.compute(new ExpressionAdapter() {
            @Override
            public Expression visitBinaryExpr(BinaryExpression expr) {
                return (expr.getOp() == And || expr.getOp() == Or)  ? null : super.visitBinaryExpr(expr);
            }

            @Override
            public Expression visitUnaryExpr(UnaryExpression expr) {
                return expr.getOp() == Not ? null : super.visitUnaryExpr(expr);
            }
        }) != null;
    }
}
