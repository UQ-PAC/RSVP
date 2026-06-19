package uq.pac.rsvp.policy.datalog.logic;

import org.junit.jupiter.api.Test;
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

    /**
     * Exhaustive enumeration of formulae over given terms
     */
    static List<Formula> combinations(List<Formula> terms) {
        return switch (terms.size()) {
            case 0: throw new AssertionFailedError();
            case 1: yield terms;
            case 2: yield List.of(
                    new Conjunction(terms.get(0), terms.get(1)),
                    new Disjunction(terms.get(0), terms.get(1)),
                    new Negation(new Conjunction(terms.get(0), terms.get(1))),
                    new Negation(new Disjunction(terms.get(0), terms.get(1)))
            );
            default:
                List<Formula> more = new ArrayList<>();
                for (int i = 1; i < terms.size(); i++) {
                    List<Formula> lhs = combinations(terms.subList(0, i));
                    List<Formula> rhs = combinations(terms.subList(i, terms.size()));
                    for (Formula l : lhs) {
                        for (Formula r : rhs) {
                            more.add(new Conjunction(l, r));
                            more.add(new Disjunction(l, r));
                            more.add(new Negation(new Conjunction(l, r)));
                            more.add(new Negation(new Disjunction(l, r)));
                        }
                    }
                }
                yield more;
        };
    }

    // Exhaustive testing
    @Test
    void dnf() {
        List<Formula> formulae = List.of(
                new Term<>("a"),
                new Term<>("b"),
                new Term<>("c"),
                new Term<>("d"),
                new Term<>("e"),
                new Term<>("f"));

        combinations(formulae).forEach(f -> {
            Formula dnf = DNFTransformer.transform(f);
            List<List<Formula>> partitioned = DNFTransformer.getNormalForm(f);

            partitioned.forEach(clause -> {
                clause.forEach(term -> {
                    Formula form = term instanceof Negation n ? n.getFormula() : term;
                    assertTrue(form instanceof Literal || form instanceof Term<?>);
                });
            });

            FormulaComparator comparator = new FormulaComparator(f);
            comparator.assertCompare(dnf);
        });
    }
}
