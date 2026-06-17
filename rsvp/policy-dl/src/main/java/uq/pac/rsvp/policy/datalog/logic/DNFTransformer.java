package uq.pac.rsvp.policy.datalog.logic;

import java.util.ArrayList;
import java.util.List;

import static uq.pac.rsvp.Assertion.require;

/**
 * Conversion of a formula to a negated normal form via De Morgan rules
 * Assumes:
 *  - All conjunctions and disjunctions are binary
 *  - An input formula is not expected to be a NNF
 */
public class DNFTransformer implements FormulaValueVisitor<Formula> {

    private DNFTransformer() {}

    private final static DNFTransformer TRANSFORMER = new DNFTransformer();

    public static Formula transform(Formula f) {
        return NNFTransformer.transform(f).accept(TRANSFORMER);
    }

    public static List<List<Formula>> getNormalForm(Formula formula) {
        List<Formula> conjunctions = splitDisjunctions(transform(formula));
        return conjunctions.stream()
                .map(DNFTransformer::splitConjunctions)
                .toList();
    }

    /**
     * Split a formula into individual predicates assuming the formula does not contain disjunctions
     */
    private static List<Formula> splitConjunctions(Formula f) {
        List<Formula> result = new ArrayList<>();
        f.accept(new FormulaVoidVisitor() {
            @Override
            public void visitLiteral(Literal literal) {
                result.add(literal);
            }

            @Override
            public void visitPredicate(Predicate<?> predicate) {
                result.add(predicate);
            }

            @Override
            public void visitNegation(Negation negation) {
                require(negation.getFormula() instanceof Literal ||
                        negation.getFormula() instanceof Predicate<?>);
                result.add(negation);
            }

            @Override
            public void visitConjunction(Conjunction conjunction) {
                conjunction.getFormulae().forEach(f -> f.accept(this));
            }

            @Override
            public void visitDisjunction(Disjunction disjunction) {
                throw new AssertionError("Unexpected disjunction: " + disjunction);
            }
        });
        return result;
    }

    /**
     * Split a DNF formula into a list of conjunctions or disjunctions assuming that
     * the input formula is in a DNF.
     */
    private static List<Formula> splitDisjunctions(Formula f) {
        List<Formula> result = new ArrayList<>();
        // Generate the list of conjunctions first
        f.accept(new FormulaVoidVisitor() {
            @Override
            public void visitLiteral(Literal literal) {
                result.add(literal);
            }

            @Override
            public void visitPredicate(Predicate<?> predicate) {
                result.add(predicate);
            }

            @Override
            public void visitNegation(Negation negation) {
                require(negation.getFormula() instanceof Literal ||
                        negation.getFormula() instanceof Predicate<?>);
                result.add(negation);
            }

            @Override
            public void visitConjunction(Conjunction conjunction) {
                result.add(conjunction);
            }

            @Override
            public void visitDisjunction(Disjunction disjunction) {
                disjunction.getFormulae().forEach(f -> f.accept(this));
            }
        });
        return result;
    }

    @Override
    public Formula visitLiteral(Literal literal) {
        return literal;
    }

    @Override
    public Formula visitPredicate(Predicate<?> predicate) {
        return predicate;
    }

    @Override
    public Formula visitConjunction(Conjunction conjunction) {
        require(conjunction.arity() == 2);
        conjunction = new Conjunction(
                conjunction.get(0).accept(this),
                conjunction.get(1).accept(this));
        require(conjunction.arity() == 2);
        if (conjunction.get(0) instanceof Disjunction d) {
            d = new Disjunction(
                    new Conjunction(d.get(0), conjunction.get(1)),
                    new Conjunction(d.get(1), conjunction.get(1)));
            return d.accept(this);
        } else if (conjunction.get(1) instanceof Disjunction d) {
            d = new Disjunction(
                    new Conjunction(conjunction.get(0), d.get(0)),
                    new Conjunction(conjunction.get(0), d.get(1)));
            return d.accept(this);
        }
        return conjunction;
    }

    @Override
    public Formula visitDisjunction(Disjunction disjunction) {
        require(disjunction.arity() == 2);
        return new Disjunction(
                disjunction.get(0).accept(this),
                disjunction.get(1).accept(this));
    }

    @Override
    public Formula visitNegation(Negation negation) {
        return new Negation(negation.getFormula().accept(this));
    }
}
