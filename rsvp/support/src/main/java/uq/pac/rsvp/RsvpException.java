/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp;

public class RsvpException extends Exception {

    public RsvpException() {
        super();
    }

    public RsvpException(String message) {
        super(message);
    }

    public RsvpException(Throwable cause) {
        super(cause);
    }

    public RsvpException(String message, Throwable cause) {
        super(message, cause);
    }

}
