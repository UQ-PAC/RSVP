package uq.pac.rsvp.policy.datalog.analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class RequestIndex {
    private final Map<String, HashMultimap<Request, String>> indices;
    private final HashMultimap<Request, String> permitIndex;
    private final HashMultimap<Request, String> forbidIndex;

    public RequestIndex(RequestAuth auth) {
        this.permitIndex = HashMultimap.create();
        this.forbidIndex = HashMultimap.create();
        this.indices = new HashMap<>();

        auth.getPermitPolicies().forEach((p, rs) -> {
            rs.forEach(r -> permitIndex.put(r, p));
            require(!indices.containsKey(p), "Duplicate policy name: " + p);
            indices.put(p, permitIndex);
        });

        auth.getForbidPolicies().forEach((p, rs) -> {
            rs.forEach(r -> forbidIndex.put(r, p));
            require(!indices.containsKey(p), "Duplicate policy name: " + p);
            indices.put(p, forbidIndex);
        });
    }

    public Multimap<String, Request> correlation(String name, Set<Request> requests) {
        HashMultimap<Request, String> index = indices.get(name);
        HashMultimap<String, Request> correlation = HashMultimap.create();
        for (Request r : requests) {
            index.get(r).forEach(pn -> {
                if (!pn.equals(name)) {
                    correlation.put(pn, r);
                }
            });
        }
        return correlation;
    }
}
