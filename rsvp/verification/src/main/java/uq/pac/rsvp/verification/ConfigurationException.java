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
