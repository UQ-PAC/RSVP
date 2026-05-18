package uq.pac.rsvp.support.error;

import uq.pac.rsvp.support.SourceLoc;

public class TranslationError extends LocationError {
    public TranslationError(String message, SourceLoc location) {
        super(message, "Translation", location);
    }

    public TranslationError(String message) {
        this(message, SourceLoc.MISSING);
    }

    public TranslationError() {
        this(null, SourceLoc.MISSING);
    }
}
