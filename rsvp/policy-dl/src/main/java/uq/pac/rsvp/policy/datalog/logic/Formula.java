/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public abstract class Formula {
    // toString cache
    private String cache = null;

    protected abstract String stringify();

    @Override
    public final String toString() {
        if (cache == null) {
            cache = stringify();
        }
        return cache;
    }

    @Override
    public final boolean equals(Object other) {
        if (other instanceof Formula f) {
            return this.toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    public abstract <T> T accept(FormulaValueVisitor<T> visitor);

    public abstract void accept(FormulaVoidVisitor visitor);

    /**
     * Return true if the expression is a (potentially negated) literal or term
     */
    public boolean isScalar() {
        Formula formula = this;
        if (formula instanceof Negation n) {
            formula = n.getFormula();
        }
        return formula instanceof Literal || formula instanceof Term;
    }
}
