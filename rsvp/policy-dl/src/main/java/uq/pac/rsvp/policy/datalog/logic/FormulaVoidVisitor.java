package uq.pac.rsvp.policy.datalog.logic;

public interface FormulaVoidVisitor {
    void visitLiteral(Literal literal);

    void visitPredicate(Term<?> term);

    void visitNegation(Negation negation);

    void visitConjunction(Conjunction conjunction);

    void visitDisjunction(Disjunction disjunction);
}
