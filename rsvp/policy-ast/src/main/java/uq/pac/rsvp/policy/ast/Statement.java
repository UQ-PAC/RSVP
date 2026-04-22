package uq.pac.rsvp.policy.ast;

import uq.pac.rsvp.support.SourceLoc;

public abstract class Statement extends PolicyAstNode {

    public Statement(SourceLoc location) {
        super(location);
    }

}
