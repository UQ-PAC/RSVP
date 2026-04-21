package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.support.SourceLoc;

public abstract class AstNode {

    private final SourceLoc source;

    protected AstNode(SourceLoc source) {
        this.source = source;
    }

    public final SourceLoc getSourceLoc() {
        return source != null ? source : SourceLoc.MISSING;
    }
}
