/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema;

public class SchemaResolutionException extends RuntimeException {

    public SchemaResolutionException() {
        super();
    }

    public SchemaResolutionException(String message) {
        super(message);
    }

    public SchemaResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaResolutionException(Throwable cause) {
        super(cause);
    }
}
