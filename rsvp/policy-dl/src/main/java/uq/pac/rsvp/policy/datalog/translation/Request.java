package uq.pac.rsvp.policy.datalog.translation;

/**
 * A class representing a cedar request as a triple {@code} (principal, resource action) {@code}.
 * <p>
 * Internally a request is represented as a single string, where components are separated
 * by '|' and double quotes are omitted.
 * <p>
 * For performance reasons validation of the strings the requests are constructed
 * from is omitted.
 */
public class Request {
    private final String request;
    private static final String DELIMITER = TranslationConstants.OUTPUT_DELIMITER;

    public Request(String request) {
        this.request = request;
    }

    public Request(String principal, String resource, String action) {
        this.request = principal + DELIMITER + resource + DELIMITER + action;
    }

    public static Request of(String request) {
        return new Request(request);
    }

    public static Request of(String principal, String resource, String action) {
        return new Request(principal, resource, action);
    }

    @Override
    public int hashCode() {
        return request.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Request req) {
            return req.request.equals(this.request);
        }
        return false;
    }

    @Override
    public String toString() {
        return request.replaceAll("\\" + DELIMITER, "\t");
    }
}
