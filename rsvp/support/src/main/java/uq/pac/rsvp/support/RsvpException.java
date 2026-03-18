package uq.pac.rsvp.support;

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
