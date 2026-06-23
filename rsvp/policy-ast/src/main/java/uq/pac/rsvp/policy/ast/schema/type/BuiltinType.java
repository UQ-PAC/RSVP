/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.SchemaAstNode;
import uq.pac.rsvp.support.SourceLoc;

public abstract class BuiltinType extends SchemaAstNode {
    protected BuiltinType(SourceLoc location) {
        super(location);
    }
}
