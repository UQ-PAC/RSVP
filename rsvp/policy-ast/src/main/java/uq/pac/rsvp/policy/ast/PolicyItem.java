package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

public abstract class PolicyItem extends AstNode {

    public PolicyItem(SourceLoc location) {
        super(location);
    }

    public abstract void accept(PolicyVisitor visitor);

    public abstract <T> T compute(PolicyComputationVisitor<T> visitor);
}
