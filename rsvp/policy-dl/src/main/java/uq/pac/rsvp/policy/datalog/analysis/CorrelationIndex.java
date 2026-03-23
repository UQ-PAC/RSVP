package uq.pac.rsvp.policy.datalog.analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestAuth;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;

import java.util.HashMap;
import java.util.Map;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Computing correlation over different sets of requests.
 * This computation determines whether some policy (represented by a set of requests)
 * overlaps or is subsumed by other policies
 */
public class CorrelationIndex {
    private final Map<String, HashMultimap<Request, String>> indices;
    private final HashMultimap<Request, String> permitIndex;
    private final HashMultimap<Request, String> forbidIndex;

    public CorrelationIndex(RequestAuth auth) {
        this.permitIndex = HashMultimap.create();
        this.forbidIndex = HashMultimap.create();
        this.indices = new HashMap<>();

        auth.getPermitPolicies().forEach(rs -> {
            rs.forEach(r -> permitIndex.put(r, rs.getName()));
            require(!indices.containsKey(rs.getName()), "Duplicate policy name: " + rs.getName());
            indices.put(rs.getName(), permitIndex);
        });

        auth.getForbidPolicies().forEach(rs -> {
            rs.forEach(r -> forbidIndex.put(r, rs.getName()));
            require(!indices.containsKey(rs.getName()), "Duplicate policy name: " + rs.getName());
            indices.put(rs.getName(), forbidIndex);
        });
    }

    public Multimap<Request, String> correlation(RequestSet requests) {
        HashMultimap<Request, String> index = indices.get(requests.getName());
        HashMultimap<Request, String> correlation = HashMultimap.create();
        requests.forEach(request -> {
            index.get(request).forEach(pn -> {
                if (!pn.equals(requests.getName())) {
                    correlation.put(request, pn);
                }
            });
        });
        return correlation;
    }
}
