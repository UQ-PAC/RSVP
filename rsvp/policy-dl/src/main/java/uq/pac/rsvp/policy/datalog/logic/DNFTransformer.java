package uq.pac.rsvp.policy.datalog.logic;

import static uq.pac.rsvp.Assertion.require;

/**
 * Conversion of a formula to a negated normal form via De Morgan rules
 */
public class DNFTransformer implements FormulaVisitor<Formula> {

    private DNFTransformer() {}

    private final static DNFTransformer TRANSFORMER = new DNFTransformer();

    public static Formula transform(Formula f) {
        return NNFTransformer.transform(f).accept(TRANSFORMER);
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
        if (conjunction.get(0) instanceof Disjunction d) {
            return new Disjunction(
                    new Conjunction(d.get(0), conjunction.get(1)),
                    new Conjunction(d.get(1), conjunction.get(1))).accept(this);
        } else if (conjunction.get(1) instanceof Disjunction d) {
            return new Disjunction(
                    new Conjunction(conjunction.get(0), d.get(0)),
                    new Conjunction(conjunction.get(0), d.get(1))).accept(this);
        } else {
            return new Conjunction(
                    conjunction.get(0).accept(this),
                    conjunction.get(1).accept(this));
        }
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
