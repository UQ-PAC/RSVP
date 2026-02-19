package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public abstract class PolicyFileEntry {

    private final SourceLoc source;

    protected PolicyFileEntry(SourceLoc source) {
        this.source = source;
    }

    public final SourceLoc getSourceLoc() {
        return source != null ? source : SourceLoc.MISSING;
    }

    public abstract void accept(PolicyVisitor visitor);

    public abstract <T> T compute(PolicyComputationVisitor<T> visitor);

}
