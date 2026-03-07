package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.value.EntityUID;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.OUTPUT_DELIMITER;

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
    private final String id;
    private final String principal;
    private final String resource;
    private final String action;

    public Request(String id) {
        this.id = id;
        String [] components = id.split(OUTPUT_DELIMITER);
        if (components.length != 3) {
            throw new TranslationError("Invalid request format: %s", id);
        }
        principal = components[0];
        resource = components[1];
        action = components[2];
    }

    public Request(String principal, String resource, String action) {
        this.id = principal + OUTPUT_DELIMITER + resource + OUTPUT_DELIMITER + action;
        this.principal = principal;
        this.resource = resource;
        this.action = action;
    }

    public static Request of(String request) {
        return new Request(request);
    }

    public static Request of(String principal, String resource, String action) {
        return new Request(principal, resource, action);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Request req) {
            return req.id.equals(this.id);
        }
        return false;
    }

    private EntityUID toCedarFormat(String entity) {
        int i = entity.lastIndexOf(':');
        String cedarForm = entity.substring(0, i + 1) + "\"" + entity.substring(i + 1) + "\"";
        return EntityUID.parse(cedarForm).orElseThrow(TranslationError::new);
    }

    /**
     * Construct a cedar-level authorisation request out of this one
     */
    public AuthorizationRequest getCedarRequest(com.cedarpolicy.model.schema.Schema schema) {
        return new AuthorizationRequest(
                toCedarFormat(principal),
                toCedarFormat(resource),
                toCedarFormat(action),
                Optional.of(Map.of()),
                Optional.of(schema),
                true);
    }

    public String getPrincipal() {
        return principal;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "%s\t%s\t%s".formatted(principal, resource, action);
    }
}
