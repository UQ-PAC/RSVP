package uq.pac.rsvp.support.error;

import uq.pac.rsvp.support.SourceLoc;

public class ValidationError extends LocationError {
    public ValidationError(String message, SourceLoc location) {
        super(message, "Validation", location);
    }

    public ValidationError(String message) {
        this(message, SourceLoc.MISSING);
    }

    public ValidationError() {
        this(null, SourceLoc.MISSING);
    }
}
