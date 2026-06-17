package uq.pac.rsvp.policy.datalog.logic;

public class FormulaVoidVisitorAdapter implements FormulaVoidVisitor {
    @Override
    public void visitLiteral(Literal literal) {}

    @Override
    public void visitPredicate(Predicate<?> predicate) {}

    @Override
    public void visitNegation(Negation negation) {
        negation.getFormula().accept(this);
    }

    @Override
    public void visitConjunction(Conjunction conjunction) {
        conjunction.formulae().forEach(f -> f.accept(this));
    }

    @Override
    public void visitDisjunction(Disjunction disjunction) {
        disjunction.formulae().forEach(f -> f.accept(this));
    }
}
