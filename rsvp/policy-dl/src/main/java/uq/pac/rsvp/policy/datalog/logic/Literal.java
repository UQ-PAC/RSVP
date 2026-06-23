/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

public class Literal extends Formula {
    private final boolean value;

    private Literal(boolean value) {
        this.value = value;
    }

    public static final Literal TRUE = new Literal(true);
    public static final Literal FALSE = new Literal(false);

    protected String stringify() {
        return Boolean.toString(value);
    }

    @Override
    public <T> T accept(FormulaValueVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public void accept(FormulaVoidVisitor listener) {
        listener.visitLiteral(this);
    }

    public boolean asBoolean() {
        return value;
    }

    public static Literal get(boolean value) {
        return value ? TRUE : FALSE;
    }
}
