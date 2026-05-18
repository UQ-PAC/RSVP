package uq.pac.rsvp.support.error;

import uq.pac.rsvp.support.SourceLoc;

public class ParseError extends LocationError {
    public ParseError(String msg, SourceLoc location) {
        super(msg, "Parse", location);
    }
}
