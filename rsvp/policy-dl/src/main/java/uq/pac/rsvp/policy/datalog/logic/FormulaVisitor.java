package uq.pac.rsvp.policy.datalog.logic;

public interface FormulaVisitor<T> {
    T visitLiteral(Literal literal);

    T visitPredicate(Predicate<?> predicate);

    T visitNegation(Negation negation);

    T visitConjunction(Conjunction conjunction);

    T visitDisjunction(Disjunction disjunction);
}
