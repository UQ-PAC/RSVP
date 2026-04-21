package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public interface PolicyItem {
    void accept(PolicyVisitor visitor);

    <T> T compute(PolicyComputationVisitor<T> visitor);
}
