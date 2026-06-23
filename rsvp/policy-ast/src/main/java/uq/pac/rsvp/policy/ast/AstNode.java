/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.error.TranslationError;

public abstract class AstNode {

    private final SourceLoc source;

    protected AstNode(SourceLoc source) {
        this.source = source;
    }

    public final SourceLoc getSourceLoc() {
        return source != null ? source : SourceLoc.MISSING;
    }

    public static TranslationError unsupported(AstNode node) {
        throw new TranslationError("Unsupported element: " + node, node.getSourceLoc());
    }
}
