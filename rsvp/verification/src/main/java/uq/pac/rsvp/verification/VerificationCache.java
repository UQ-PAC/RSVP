package uq.pac.rsvp.verification;

import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.verification.impact.ChangeImpact;

import java.util.HashMap;
import java.util.Map;

public class VerificationCache {
    private final Map<String, Map<Request, RequestResult>> requestMappings;
    private final Map<String, Map<String, ChangeImpact>> changeImpacts;
    private final FileSet fileset;

    public VerificationCache(FileSet fileset) {
        requestMappings = new HashMap<>();
        changeImpacts = new HashMap<>();

        // TODO: check that fileset is finalised
        this.fileset = fileset;
    }

    public ChangeImpact getImpact(String original, String updated) {
        Map<String, ChangeImpact> impacts = changeImpacts.get(original);

        if (impacts == null) {
            return null;
        }

        return impacts.get(updated);
    }

    public Map<Request, RequestResult> getMapping(String file) {
        return requestMappings.get(file);
    }

    public FileSet getFileset() {
        return fileset;
    }

    public void cacheImpact(String original, String updated, ChangeImpact impacts) {
        changeImpacts.computeIfAbsent(original, key -> new HashMap<>()).put(updated, impacts);
    }

    public void cacheMapping(String policyVersion, Map<Request, RequestResult> mapping) {
        requestMappings.put(policyVersion, mapping);
    }

}
