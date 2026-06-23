/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

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
