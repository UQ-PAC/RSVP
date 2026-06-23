/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public interface FormulaVoidVisitor {
    void visitLiteral(Literal literal);

    void visitPredicate(Term<?> term);

    void visitNegation(Negation negation);

    void visitConjunction(Conjunction conjunction);

    void visitDisjunction(Disjunction disjunction);
}
