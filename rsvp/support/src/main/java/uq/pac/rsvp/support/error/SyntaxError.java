package uq.pac.rsvp.support.error;

import uq.pac.rsvp.support.SourceLoc;

public class SyntaxError extends LocationError {
    public SyntaxError(String msg, SourceLoc location) {
        super(msg, "Syntax", location);
    }
}
