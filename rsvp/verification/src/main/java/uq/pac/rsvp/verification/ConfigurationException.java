/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.verification;

public class ConfigurationException extends Exception {


    public ConfigurationException() {
        super("Invalid configuration");
    }

    public ConfigurationException(String message) {
        super("Invalid configuration: " + message);
    }

    public ConfigurationException(Throwable cause) {
        super("Invalid configuration");
    }

    public ConfigurationException(String message, Throwable cause) {
        super("Invalid configuration: " + message, cause);
    }
}
