/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public class Disjunction extends LogicalOperator {
    public Disjunction(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    String getStringOperator() {
        return "||";
    }

    @Override
    public <T> T accept(FormulaValueVisitor<T> visitor) {
        return visitor.visitDisjunction(this);
    }

    @Override
    public void accept(FormulaVoidVisitor listener) {
        listener.visitDisjunction(this);
    }
}
