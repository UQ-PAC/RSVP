/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public class IpAddressType extends BuiltinType {

    public IpAddressType(SourceLoc location) {
        super(location);
    }

    public IpAddressType() {
        this(SourceLoc.MISSING);
    }

    @Override
    public String toString() {
        return "__cedar::ipaddr";
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitIpAddress(this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IpAddressType;
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitIpAddress(this);
    }

    @Override
    public <T> void accept(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitIpAddress(this, payload);
    }
}
