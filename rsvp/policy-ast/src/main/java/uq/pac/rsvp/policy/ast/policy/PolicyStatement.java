package uq.pac.rsvp.policy.ast.policy;

import uq.pac.rsvp.support.SourceLoc;

public abstract class PolicyStatement extends PolicyAstNode {

    public PolicyStatement(SourceLoc location) {
        super(location);
    }

}
