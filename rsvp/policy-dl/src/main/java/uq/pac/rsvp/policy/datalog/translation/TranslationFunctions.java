package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Registry of classes for translating cedar-level functions to Datalog
 */
public abstract class TranslationFunctions {
    static abstract class Function {
        /**
         * Function implementation
         */
        public abstract List<DLRuleExpr> translate(CallExpression expression, boolean negated, OperandVisitor operands);

        /**
         * Contexts in which the function is valid
         */
        public abstract Set<TranslationContext> getContext();
    }

    private static final Map<String, Supplier<? extends Function>> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put("isEmpty", IsEmptyFunction::new);
        REGISTRY.put("contains", ContainsFunction::new);
        REGISTRY.put("containsAll", ContainsAllFunction::new);
        REGISTRY.put("containsAny", ContainsAnyFunction::new);
        REGISTRY.put("deny", DenyFunction::new);
        REGISTRY.put("allow", AllowFunction::new);
    }

    static Function getFunction(String name, TranslationContext context) {
        Function fun = REGISTRY.get(name).get();
        if (fun != null && fun.getContext().contains(context)) {
            return fun;
        }
        return null;
    }

    /**
     * Validating call expressions
     */
    private static void validate(CallExpression expr, boolean object, int arguments) {
        if (object && expr.getSelf() == null) {
            throw new TranslationError("Function %s requires non-null self object".formatted(expr.getFunc()));
        }

        if (!object && expr.getSelf() != null) {
            throw new TranslationError("Function %s requires null self object".formatted(expr.getFunc()));
        }

        if (expr.getArgs().size() != arguments) {
            throw new TranslationError("Function %s expects %d arguments".formatted(expr.getFunc(), expr.getArgs().size()));
        }
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
            validate(expr, true, 0);

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

        @Override
        public Set<TranslationContext> getContext() {
            return Set.of(TranslationContext.Policy, TranslationContext.Invariant);
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
            validate(expr, true, 1);

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

        @Override
        public Set<TranslationContext> getContext() {
            return Set.of(TranslationContext.Policy, TranslationContext.Invariant);
        }
    }

    static class ContainsAllFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            // Expect a single argument here and only in the form of property access
            // Literal-set variation is re-written to disjunctions over contains
            validate(expr, true, 1);

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

        @Override
        public Set<TranslationContext> getContext() {
            return Set.of(TranslationContext.Policy, TranslationContext.Invariant);
        }
    }

    static class ContainsAnyFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            // Expect a single argument here and only in the form of property access
            // Literal-set variation is re-written to disjunctions over contains
            validate(expr, true, 1);

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

        @Override
        public Set<TranslationContext> getContext() {
            return Set.of(TranslationContext.Policy, TranslationContext.Invariant);
        }
    }

    // deny(principal, resource, action)
    // This function is equivalent of ForbiddenRequests(principal, resource, action)
    // i.e., in Invariant context it allows to bring in all forbidden requests
    static class DenyFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            validate(expr, false, 3);
            DLTerm principal = expr.getArgs().get(0).compute(operands),
                    resource = expr.getArgs().get(1).compute(operands),
                    action = expr.getArgs().get(2).compute(operands);
            DLRuleDecl decl = negated ? PermittedRequestsRuleDecl : ForbiddenRequestsRuleDecl;
            return List.of(
                    new DLAtom(ActionableRequestsRuleDecl, principal, resource, action),
                    new DLAtom(decl, principal, resource, action));
        }

        @Override
        public Set<TranslationContext> getContext() {
            return Set.of(TranslationContext.Invariant);
        }
    }

    // allow(principal, resource, action)
    // This function is equivalent of PermittedRequests(principal, resource, action)
    // i.e., in Invariant context it allows to bring in all permitted requests
    static class AllowFunction extends Function {
        @Override
        public List<DLRuleExpr> translate(CallExpression expr, boolean negated, OperandVisitor operands) {
            validate(expr, false, 3);
            DLTerm principal = expr.getArgs().get(0).compute(operands),
                    resource = expr.getArgs().get(1).compute(operands),
                    action = expr.getArgs().get(2).compute(operands);
            DLRuleDecl decl = negated ? ForbiddenRequestsRuleDecl : PermittedRequestsRuleDecl;
            return List.of(
                    new DLAtom(ActionableRequestsRuleDecl, principal, resource, action),
                    new DLAtom(decl, principal, resource, action));
        }

        @Override
        public Set<TranslationContext> getContext() {
            return Set.of(TranslationContext.Invariant);
        }
    }
}
