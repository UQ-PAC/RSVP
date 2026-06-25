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
    private final String error;

    public LocationError(String error, String title, SourceLoc location) {
        super(error + " at: " + location.toString());
        this.location = location;
        this.title = title;
        this.error = error;
    }

    public SourceLoc getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Return the error message without the location information
     */
    public String getErrorMessage() {
        return error;
    }
}
