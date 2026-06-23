/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public class Negation extends Formula {
    private final Formula formula;

    public Negation(Formula formula) {
        this.formula = formula;
    }

    protected String stringify() {
        return "!" + formula.toString();
    }

    @Override
    public <T> T accept(FormulaValueVisitor<T> visitor) {
        return visitor.visitNegation(this);
    }

    @Override
    public void accept(FormulaVoidVisitor listener) {
        listener.visitNegation(this);
    }

    public Formula getFormula() {
        return formula;
    }
}
