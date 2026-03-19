package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.AttributeRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.HasAttributeRuleDecl;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Registry of classes for translating cedar-level functions to Datalog
 */
public abstract class TranslationFunctions {
    static abstract class Function {
        public abstract List<DLRuleExpr> translate(CallExpression expression, boolean negated, OperandVisitor operands);
    }

    private static final Map<String, Supplier<? extends Function>> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put("isEmpty", IsEmptyFunction::new);
        REGISTRY.put("contains", ContainsFunction::new);
        REGISTRY.put("containsAll", ContainsAllFunction::new);
        REGISTRY.put("containsAny", ContainsAnyFunction::new);
    }

    static boolean isRegistered(String name) {
        return REGISTRY.containsKey(name);
    }

    static Function getFunction(String name) {
        return REGISTRY.get(name).get();
    }

    /**
     * <pre>
     * x.y.isEmpty() ->
     *     count : { Attribute(x, y, z) } = 0
     *     HasAttribute(x, y),
     * </pre>
     * {@code HasAttribute} is needed to avoid the negated form counting in undefined entities
     */
    static class IsEmptyFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            // isEmpty has no arguments
            require(expr.getArgs().isEmpty());

            if (expr.getSelf() instanceof PropertyAccessExpression pe) {
                DLTerm propertyTerm = DLTerm.lit(pe.getProperty());
                DLTerm set = pe.getObject().compute(operands);
                DLTerm aggregate = new DLAggregate(DLAggregate.Aggregate.COUNT,
                        new DLAtom(AttributeRuleDecl, set, DLTerm.lit(pe.getProperty()), new DLVar("_")));
                DLConstraint.Operator op = negated ? DLConstraint.Operator.GT : DLConstraint.Operator.EQ;
                return List.of(
                        new DLAtom(HasAttributeRuleDecl, set, propertyTerm),
                        new DLConstraint(aggregate, DLTerm.lit(0), op));
            } else {
                throw new TranslationError("Unsupported set.isEmpty() form: " + expr);
            }
        }
    }

    /**
     *  <pre>
     *  x.y.contains(z) form ->
     *      count : { Attribute(x, y, z) } >= 1
     *      HasAttribute(x, y)
     *  </pre>
     * {@code HasAttribute} is needed to avoid the negated form counting in undefined entities
     */
    static class ContainsFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            // Contains uses a single argument
            require(expr.getArgs().size() == 1);
            DLTerm argument = expr.getArgs().getFirst().compute(operands);

            if (expr.getSelf() instanceof PropertyAccessExpression pe) {
                DLTerm propertyTerm = DLTerm.lit(pe.getProperty());
                DLTerm set = pe.getObject().compute(operands);
                // Count term
                DLTerm aggregate = new DLAggregate(DLAggregate.Aggregate.COUNT,
                        new DLAtom(AttributeRuleDecl, set, propertyTerm, argument));
                // Negation-based operator
                DLConstraint.Operator op = negated ? DLConstraint.Operator.LT : DLConstraint.Operator.GTE;
                return List.of(
                        new DLAtom(HasAttributeRuleDecl, set, propertyTerm),
                        new DLConstraint(aggregate, DLTerm.lit(1), op));
            } else {
                throw new TranslationError("Unsupported set.contains() form: " + expr);
            }
        }
    }

    static class ContainsAllFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            // Expect a single argument here and only in the form of property access
            // Literal-set variation is re-written to disjunctions over contains
            require(expr.getArgs().size() == 1);

            Expression arg = expr.getArgs().getFirst();

            if (expr.getSelf() instanceof PropertyAccessExpression lpe && arg instanceof PropertyAccessExpression rpe) {
                DLTerm leftPropertyTerm = DLTerm.lit(lpe.getProperty());
                DLTerm leftSet = lpe.getObject().compute(operands);
                DLTerm rightPropertyTerm = DLTerm.lit(rpe.getProperty());
                DLTerm rightSet = rpe.getObject().compute(operands);
                DLTerm connector = operands.getTmpVar();

                DLTerm aggregateLhs = new DLAggregate(DLAggregate.Aggregate.COUNT,
                        new DLAtom(AttributeRuleDecl, leftSet, leftPropertyTerm, connector),
                        new DLAtom(AttributeRuleDecl, rightSet, rightPropertyTerm, connector));

                DLTerm aggregateRhs = new DLAggregate(DLAggregate.Aggregate.COUNT,
                        new DLAtom(AttributeRuleDecl, rightSet, rightPropertyTerm, new DLVar("_")));

                // Negation-based operator
                DLConstraint.Operator op = negated ? DLConstraint.Operator.NEQ : DLConstraint.Operator.EQ;
                return List.of(
                        new DLAtom(HasAttributeRuleDecl, leftSet, leftPropertyTerm),
                        new DLAtom(HasAttributeRuleDecl, rightSet, rightPropertyTerm),
                        new DLConstraint(aggregateLhs, aggregateRhs, op));
            } else {
                throw new TranslationError("Unsupported set.contains() form: " + expr);
            }
        }
    }

    static class ContainsAnyFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            // Expect a single argument here and only in the form of property access
            // Literal-set variation is re-written to disjunctions over contains
            require(expr.getArgs().size() == 1);

            Expression arg = expr.getArgs().getFirst();

            if (expr.getSelf() instanceof PropertyAccessExpression lpe && arg instanceof PropertyAccessExpression rpe) {
                DLTerm leftPropertyTerm = DLTerm.lit(lpe.getProperty());
                DLTerm leftSet = lpe.getObject().compute(operands);
                DLTerm rightPropertyTerm = DLTerm.lit(rpe.getProperty());
                DLTerm rightSet = rpe.getObject().compute(operands);
                DLTerm connector = operands.getTmpVar();

                // Count term
                DLTerm aggregate = new DLAggregate(DLAggregate.Aggregate.COUNT,
                        new DLAtom(AttributeRuleDecl, leftSet, leftPropertyTerm, connector),
                        new DLAtom(AttributeRuleDecl, rightSet, rightPropertyTerm, connector));

                // Negation-based operator
                DLConstraint.Operator op = negated ? DLConstraint.Operator.LT : DLConstraint.Operator.GTE;
                return List.of(
                    new DLAtom(HasAttributeRuleDecl, leftSet, leftPropertyTerm),
                    new DLAtom(HasAttributeRuleDecl, rightSet, rightPropertyTerm),
                    new DLConstraint(aggregate, DLTerm.lit(1), op));
            } else {
                throw new TranslationError("Unsupported set.contains() form: " + expr);
            }
        }
    }
}
