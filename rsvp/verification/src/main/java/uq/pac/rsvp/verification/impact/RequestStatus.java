package uq.pac.rsvp.verification.impact;

/**
 * A request (string with principal, action and resource) and its status (permitted or
 * forbidden)
 */
public class RequestStatus {
    private final String request;
    private final boolean status;

    public String getRequest() {
        return request;
    }

    public boolean isPermitted() {
        return status;
    }

    public boolean isForbidden() {
        return !status;
    }

    public RequestStatus(String request, boolean status) {
        this.request = request;
        this.status = status;
    }

    public String toString() {
        return (status ? " +  " : " -  ") + request;
    }
}
