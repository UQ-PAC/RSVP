package uq.pac.rsvp.policy.datalog.logic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.AssertionFailedError;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uq.pac.rsvp.Assertion.require;

public class FormulaTest {

    // Parse formula from string
    static Formula parse(String expr) {
        return Expression.parse(expr).compute(new PolicyComputationVisitor<>() {
            @Override
            public Formula visitBinaryExpr(BinaryExpression expr) {
                Formula lhs = expr.getLeft().compute(this);
                Formula rhs = expr.getRight().compute(this);
                return switch (expr.getOperator()) {
                    case Or -> new Disjunction(lhs, rhs);
                    case And -> new Conjunction(lhs, rhs);
                    default -> throw new AssertionFailedError("Unexpected");
                };
            }

            @Override
            public Formula visitUnaryExpr(UnaryExpression expr) {
                if (expr.getOperator() == UnaryExpression.Operator.Not) {
                    return new Negation(expr.getExpression().compute(this));
                }
                throw new AssertionFailedError("Unexpected");
            }

            @Override
            public Formula visitVariableExpr(VariableExpression expr) {
                return new Term<>(expr.getReference());
            }

            @Override
            public Formula visitBooleanExpr(BooleanExpression expr) {
                return expr.getValue() ? Literal.TRUE : Literal.FALSE;
            }
        });
    }

    static class FormulaComparator {
        private final List<Term<?>> terms;
        private final List<List<Boolean>> inputs;
        private final Formula formula;

        FormulaComparator(Formula formula) {
            this.formula = formula;
            this.terms = getPredicates(formula);
            this.inputs = getInputs(terms.size());
        }

        /* Get a list of unique predicates of a formula */
        public static List<Term<?>> getPredicates(Formula f) {
            Set<Term<?>> terms = new HashSet<>();
            f.accept(new FormulaVoidVisitorAdapter() {
                @Override
                public void visitPredicate(Term<?> term) {
                    terms.add(term);
                }
            });
            return terms.stream().toList();
        }

        public static List<List<Boolean>> getInputs(int predicates) {
            List<List<Boolean>> inputs = new ArrayList<>();
            inputs.add(new ArrayList<>());
            for (int i = 0; i < predicates; i++) {
                int size = inputs.size();
                for (int j = 0; j < size; j++) {
                    List<Boolean> prev = inputs.get(j);
                    List<Boolean> next = new ArrayList<>(prev);
                    prev.add(true);
                    next.add(false);
                    inputs.add(next);
                }
            }
            return inputs;
        }

        private boolean execute(Formula formula, Map<Term<?>, Boolean> index) {
            return formula.accept(new FormulaValueVisitor<>() {
                @Override
                public Boolean visitLiteral(Literal literal) {
                    return literal.asBoolean();
                }

                @Override
                public Boolean visitPredicate(Term<?> term) {
                    assertTrue(index.containsKey(term),
                            "Predicate %s missing from index: " + term.stringify());
                    return index.get(term);
                }

                @Override
                public Boolean visitNegation(Negation negation) {
                    return !negation.getFormula().accept(this);
                }

                @Override
                public Boolean visitConjunction(Conjunction conjunction) {
                    require(conjunction.arity() > 1);
                    for (Formula f : conjunction.getFormulae()) {
                        if (!f.accept(this)) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public Boolean visitDisjunction(Disjunction disjunction) {
                    require(disjunction.arity() > 1);
                    for (Formula f : disjunction.getFormulae()) {
                        if (f.accept(this)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        void assertCompare(Formula another) {
            for (List<Boolean> input : inputs) {
                Map<Term<?>, Boolean> index = new HashMap<>();
                for (int i = 0; i < terms.size(); i++) {
                    index.put(terms.get(i), input.get(i));
                }
                boolean f1 = execute(formula, index);
                boolean f2 = execute(another, index);
                assertEquals(f1, f2, "Result mismatch on argument: " + index);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "(a && b) || c, ((a && b) || c)",
            "(a || b) && c, ((a && c) || (b && c))",
            "a && (b || c), ((a && b) || (a && c))",
            "(a || b) && (c || d), (((a && c) || (a && d)) || ((b && c) || (b && d)))",
            "(a && b) && (c || d), (((a && b) && c) || ((a && b) && d))",
            "(a || b || c) && d, (((a && d) || (b && d)) || (c && d))",
            "(a && (b && (c || d))), ((a && (b && c)) || (a && (b && d)))",
            "(((c || d) && a) && b),(((c && a) && b) || ((d && a) && b)) "
    })
    void dnf(String input, String expected) {
        Formula f = parse(input);
        Formula dnf = DNFTransformer.transform(f);
        FormulaComparator comparator = new FormulaComparator(f);
        comparator.assertCompare(dnf);
        assertEquals(expected, dnf.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "!(a || b), (!a && !b)",
            "!(a && b), (!a || !b)",
            "!(!(a && b) || (!c && !d)), ((a && b) && (c || d)) ",
            "!(c || !(b && !a)), (!c && (b && !a)) "
    })
    void nnf(String input, String expected) {
        Formula f = parse(input);
        Formula dnf = NNFTransformer.transform(f);
        assertEquals(expected, dnf.toString());
    }
}
