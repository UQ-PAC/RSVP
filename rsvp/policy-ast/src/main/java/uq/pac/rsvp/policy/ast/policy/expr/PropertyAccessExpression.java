/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class PropertyAccessExpression extends Expression {

    private final Expression obj;
    private final String prop;

    public PropertyAccessExpression(Expression obj, String prop, SourceLoc source) {
        super(source);
        this.obj = obj;
        this.prop = prop;
    }

    public PropertyAccessExpression(Expression obj, String prop) {
        this(obj, prop, SourceLoc.MISSING);
    }

    public Expression getObject() {
        return obj;
    }

    public String getProperty() {
        return prop;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitPropertyAccessExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitPropertyAccessExpr(this);
    }

    @Override
    public <T, P> T compute(PolicyPayloadVisitor<T, P> visitor, P payload) {
        return visitor.visitPropertyAccessExpr(this, payload);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(obj.toString());

        if (NICE_PROP_NAME.matcher(prop).matches()) {
            sb.append('.');
            sb.append(prop);
        } else {
            sb.append("[\"");
            sb.append(prop);
            sb.append("\"]");
        }

        return sb.toString();
    }

}
