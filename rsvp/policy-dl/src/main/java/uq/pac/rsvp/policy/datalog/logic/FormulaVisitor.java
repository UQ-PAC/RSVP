package uq.pac.rsvp.policy.datalog.logic;

public interface FormulaVisitor<T> {
    T visitConjunction(Conjunction formula);

    T visitDisjunction(Disjunction formula);

    T visitNegation(Negation formula);

    T visitLiteral(Literal formula);

    T visitPredicate(Predicate<?> formula);
}
