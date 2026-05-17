package uq.pac.rsvp.policy.ast;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import uq.pac.rsvp.support.SourceLoc;

public class ParseError extends ParseCancellationException {
    private final SourceLoc location;

    public ParseError(String msg, SourceLoc location) {
        super(msg);
        this.location = location;
    }

    public SourceLoc getLocation() {
        return location;
    }
}
