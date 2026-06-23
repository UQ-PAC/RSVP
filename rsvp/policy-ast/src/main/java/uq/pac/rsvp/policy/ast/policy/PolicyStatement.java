/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy;

import uq.pac.rsvp.support.SourceLoc;

public abstract class PolicyStatement extends PolicyAstNode {

    public PolicyStatement(SourceLoc location) {
        super(location);
    }

}
