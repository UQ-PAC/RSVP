package uq.pac.rsvp.policy.ast.policy;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

public abstract class PolicyAstNode extends AstNode {

    public PolicyAstNode(SourceLoc location) {
        super(location);
    }

    public abstract void accept(PolicyVisitor visitor);

    public abstract <T> T compute(PolicyComputationVisitor<T> visitor);
}
