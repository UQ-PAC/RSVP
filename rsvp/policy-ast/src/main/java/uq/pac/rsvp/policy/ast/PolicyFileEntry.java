package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.support.SourceLoc;

public abstract class PolicyFileEntry implements PolicyItem {

    private final SourceLoc source;

    protected PolicyFileEntry(SourceLoc source) {
        this.source = source;
    }

    public final SourceLoc getSourceLoc() {
        return source != null ? source : SourceLoc.MISSING;
    }

}
