/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

import static uq.pac.rsvp.Assertion.require;

/**
 * Numeric literal term
 */
public final class DLNumber extends DLTerm {
    private final double number;

    private boolean isInt() {
        return number % 1 == 0;
    }

    public DLNumber(int number) {
        this.number = number;
    }

    public DLNumber(double number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLNumber s) {
            return s.number == number;
        }
        return false;
    }

    @Override
    protected String stringify() {
        return isInt() ? Integer.toString((int) number) : Double.toString(number);
    }

    public int getInt() {
        require(isInt());
        return (int) number;
    }

    public double getNumber() {
        return number;
    }
}
