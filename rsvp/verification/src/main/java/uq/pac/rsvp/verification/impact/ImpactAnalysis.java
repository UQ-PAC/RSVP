package uq.pac.rsvp.verification.impact;

import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.verification.RequestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImpactAnalysis {

    public static List<RequestStatus> computeImpact(Map<Request, RequestResult> original, Map<Request, RequestResult> updated) {

        List<RequestStatus> changeImpact = new ArrayList<>();

        // Determine which requests have changed from/to permit/forbid/uncovered;
        // Check overall numbers (newly permitted/forbidden/unmatched vs old)
        // Report which requests changed and why, for each reason;
        // reasons:
        //   - now forbidden, due to matching policy(s) 'x'
        //   - now forbidden, due to no longer matching any policies after previously
        //                    matching permit policy(s) 'x'
        //   - now permitted, due to no longer matching forbid policy(s) 'x'
        //   - no longer matches any policies (previously forbidden by policy(s) 'x')

        // For now though, simply check which requests have changed status:

        for (Map.Entry<Request, RequestResult> requestEntry : updated.entrySet()) {
            RequestResult prevResult = original.get(requestEntry.getKey());
            if ((prevResult == null && requestEntry.getValue().permitted) || (prevResult != null && requestEntry.getValue().permitted != prevResult.permitted)) {
                Request request = requestEntry.getKey();
                if (request.known()) {
                    changeImpact.add(new RequestStatus(request.toHumanReadableString(),
                            requestEntry.getValue().permitted));
                }
            }
        }

        // Also need to check requests that are permitted previously, but no longer covered:
        for (Map.Entry<Request, RequestResult> requestEntry : original.entrySet()) {
            if (requestEntry.getValue().permitted) {
                RequestResult currentResult = updated.get(requestEntry.getKey());
                if (currentResult == null) {
                    Request request = requestEntry.getKey();
                    if (request.known()) {
                        changeImpact.add(new RequestStatus(request.toHumanReadableString(), false));
                    }
                }
            }
        }

        return changeImpact;
    }
}
