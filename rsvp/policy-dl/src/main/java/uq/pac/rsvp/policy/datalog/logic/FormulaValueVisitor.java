/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public interface FormulaValueVisitor<T> {
    T visitLiteral(Literal literal);

    T visitPredicate(Term<?> term);

    T visitNegation(Negation negation);

    T visitConjunction(Conjunction conjunction);

    T visitDisjunction(Disjunction disjunction);
}
