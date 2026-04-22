package uq.pac.rsvp.policy.datalog.translation;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.translation.RequestAuth.Decision.*;
import static uq.pac.rsvp.Assertion.require;

/**
 * A class representing a request authentication engine that checks whether a
 * given request is permitted, forbidden or inconclusive.
 */
public class RequestAuth {
    /**
     * Permitted requests
     */
    private final RequestSet permitted;
    /**
     * Forbidden requests
     */
    private final RequestSet forbidden;

    public RequestAuth(Translation result) {
        this.permitted = result.loadRequests(TranslationConstants.PermittedRequestsRuleDecl);
        this.forbidden = result.loadRequests(TranslationConstants.ForbiddenRequestsRuleDecl);
        // Permitted and forbidden are disjoint sets
        permitted.forEach(s -> require(!forbidden.contains(s)));
    }

    public enum Decision {
        Allow,
        Deny,
        Invalid
    }

    public Decision authorize(Request request) {
        if (permitted.contains(request)) {
            return Allow;
        } else if (forbidden.contains(request)) {
            return Deny;
        }
        return Invalid;
    }

    public RequestSet getPermittedRequests() {
        return permitted;
    }

    public RequestSet getForbiddenRequests() {
        return forbidden;
    }

    public Set<Request> getActionableRequests() {
        Set<Request> requests = new HashSet<>();
        requests.addAll(getPermittedRequests().getRequests());
        requests.addAll(getForbiddenRequests().getRequests());
        return requests;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Permitted: \n");
        for (Request p : permitted.getRequests()) {
            sb.append("   ").append(p.toString()).append('\n');
        }
        return sb.toString();
    }
}
