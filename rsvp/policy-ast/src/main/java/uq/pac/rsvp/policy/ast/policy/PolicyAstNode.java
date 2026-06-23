/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

public abstract class PolicyAstNode extends AstNode {

    public PolicyAstNode(SourceLoc location) {
        super(location);
    }

    public abstract void accept(PolicyVisitor visitor);

    public abstract <T> T compute(PolicyComputationVisitor<T> visitor);

    public abstract <T, P> T compute(PolicyPayloadVisitor<T, P> visitor, P payload);
}
