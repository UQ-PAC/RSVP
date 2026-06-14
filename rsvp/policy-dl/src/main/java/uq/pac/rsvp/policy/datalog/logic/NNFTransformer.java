package uq.pac.rsvp.policy.datalog.logic;

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
    public Formula visitLiteral(Literal formula) {
        return formula;
    }

    @Override
    public Formula visitPredicate(Predicate<?> formula) {
        return formula;
    }

    @Override
    public Formula visitConjunction(Conjunction formula) {
        return new Conjunction(
                formula.getLeft().accept(this),
                formula.getRight().accept(this));
    }

    @Override
    public Formula visitDisjunction(Disjunction formula) {
        return new Disjunction(
                formula.getLeft().accept(this),
                formula.getRight().accept(this));
    }

    @Override
    public Formula visitNegation(Negation negation) {
        return switch (negation.getFormula()) {
            case Negation n -> n.getFormula().accept(this);
            case Conjunction c ->
                    new Disjunction(new Negation(c.getLeft()), new Negation(c.getRight())).accept(this);
            case Disjunction d ->
                    new Conjunction(new Negation(d.getLeft()), new Negation(d.getRight())).accept(this);
            default -> negation;
        };
    }
}
