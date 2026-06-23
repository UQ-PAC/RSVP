/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class BooleanExpression extends Expression {

    private final boolean value;

    public BooleanExpression(boolean value, SourceLoc source) {
        super(source);
        this.value = value;
    }

    public BooleanExpression(boolean value) {
        this(value, SourceLoc.MISSING);
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitBooleanExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitBooleanExpr(this);
    }

    @Override
    public <T, P> T compute(PolicyPayloadVisitor<T, P> visitor, P payload) {
        return visitor.visitBooleanExpr(this, payload);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BooleanExpression b) {
            return this.value == b.value;
        }
        return false;
    }
}
