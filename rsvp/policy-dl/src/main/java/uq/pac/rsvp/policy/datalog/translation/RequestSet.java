package uq.pac.rsvp.policy.datalog.translation;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Wrapper around a named set of requests.
 * This class aims to represent the requests of a policy
 */
public class RequestSet {
    private final String name;
    private final Set<Request> requests;

    public RequestSet(Set<Request> requests, String name) {
        this.name = name;
        this.requests = Set.copyOf(requests);
    }

    public Set<Request> getRequests() {
        return requests;
    }

    public String getName() {
        return name;
    }

    public Stream<Request> requests() {
        return requests.stream();
    }

    public void forEach(Consumer<Request> cs) {
        requests.forEach(cs);
    }

    public boolean contains(Request r) {
        return requests.contains(r);
    }

    public boolean isEmpty() {
        return requests.isEmpty();
    }

    public int size() {
        return requests.size();
    }
}
