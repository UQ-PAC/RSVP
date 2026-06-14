package uq.pac.rsvp.policy.datalog.logic;

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
    public Formula visitLiteral(Literal formula) {
        return formula;
    }

    @Override
    public Formula visitPredicate(Predicate<?> formula) {
        return formula;
    }

    @Override
    public Formula visitConjunction(Conjunction formula) {
        if (formula.getLeft() instanceof Disjunction d) {
            return new Disjunction(
                    new Conjunction(d.getLeft(), formula.getRight()),
                    new Conjunction(d.getRight(), formula.getRight())).accept(this);
        } else if (formula.getRight() instanceof Disjunction d) {
            return new Disjunction(
                    new Conjunction(formula.getLeft(), d.getLeft()),
                    new Conjunction(formula.getLeft(), d.getRight())).accept(this);
        } else {
            return new Conjunction(
                    formula.getLeft().accept(this),
                    formula.getRight().accept(this));
        }
    }

    @Override
    public Formula visitDisjunction(Disjunction formula) {
        return new Disjunction(
                formula.getLeft().accept(this),
                formula.getRight().accept(this));
    }

    @Override
    public Formula visitNegation(Negation negation) {
        return new Negation(negation.getFormula().accept(this));
    }
}
