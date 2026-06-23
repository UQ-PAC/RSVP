/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public class FormulaVoidVisitorAdapter implements FormulaVoidVisitor {
    @Override
    public void visitLiteral(Literal literal) {}

    @Override
    public void visitPredicate(Term<?> term) {}

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
