package uq.pac.rsvp.policy.datalog.logic;

import static uq.pac.rsvp.Assertion.require;

/**
 * Conversion of a formula to a negated normal form via De Morgan rules
 */
public class NNFTransformer implements FormulaVisitor<Formula> {

    private NNFTransformer() {}

    private final static NNFTransformer TRANSFORMER = new NNFTransformer();

    public static Formula transform(Formula f) {
        return f.accept(TRANSFORMER);
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
        return new Conjunction(
                conjunction.get(0).accept(this),
                conjunction.get(1).accept(this));
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
        return switch (negation.getFormula()) {
            case Negation n -> n.getFormula().accept(this);
            case Conjunction c ->
                    new Disjunction(new Negation(c.get(0)), new Negation(c.get(1))).accept(this);
            case Disjunction d ->
                    new Conjunction(new Negation(d.get(0)), new Negation(d.get(1))).accept(this);
            default -> negation;
        };
    }
}
