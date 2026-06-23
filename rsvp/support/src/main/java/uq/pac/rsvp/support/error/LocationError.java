/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.support.error;

import uq.pac.rsvp.support.SourceLoc;

/**
 * Top of the hierarchy exception for conveying parsing, validation translation messages
 * This includes a source location associated with an error and a title specifying the king
 * of an issue, e.g., Translation
 */
public abstract class LocationError extends RuntimeException {
    private final SourceLoc location;
    private final String title;

    public LocationError(String msg, String title, SourceLoc location) {
        super(msg + " at: " + location.toString());
        this.location = location;
        this.title = title;
    }

    public SourceLoc getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }
}
