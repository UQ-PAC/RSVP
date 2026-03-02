package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public interface PolicyItem {
    public void accept(PolicyVisitor visitor);

    public <T> T compute(PolicyComputationVisitor<T> visitor);
}
