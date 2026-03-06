package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.value.EntityUID;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Construct a cedar-level authorization request out of this one
     */
    public AuthorizationRequest getCedarRequest(com.cedarpolicy.model.schema.Schema schema) {
        List<EntityUID> uids =  getComponents().stream()
                .map(s -> {
                    int i = s.lastIndexOf(':');
                    String repr = s.substring(0, i + 1) + "\"" + s.substring(i + 1) + "\"";
                    return EntityUID.parse(repr).orElse(null);
                }).toList();

        return new AuthorizationRequest(
                uids.get(0),
                uids.get(2),
                uids.get(1),
                Optional.of(Map.of()),
                Optional.of(schema),
                true);
    }

    /**
     * Get request components as strings as [principal, resource, action] list
     */
    private List<String> getComponents() {
        return Arrays.asList(request.split("\\" + DELIMITER));
    }

    @Override
    public String toString() {
        return String.join("\t", getComponents());
    }
}
