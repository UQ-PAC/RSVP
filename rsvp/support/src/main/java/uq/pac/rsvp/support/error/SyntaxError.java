/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.support.error;

import uq.pac.rsvp.support.SourceLoc;

public class SyntaxError extends LocationError {
    public SyntaxError(String msg, SourceLoc location) {
        super(msg, "Syntax", location);
    }
}
