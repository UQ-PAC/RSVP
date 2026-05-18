package uq.pac.rsvp.verification.impact;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.verification.RequestResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImpactAnalysis {

    public static ChangeImpact computeImpact(Map<Request, RequestResult> original, Map<Request, RequestResult> updated) {

        List<RequestSummary> permitted = new ArrayList<>();
        List<RequestSummary> forbidden = new ArrayList<>();

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

        // Track unresolved requests (with ???) so results can be simplified
        Set<Request> unknownPermitted = new HashSet<>();
        Set<Request> unknownForbidden = new HashSet<>();

        // Cache known requests for simplification
        Set<Request> knownPermitted = new HashSet<>();
        Set<Request> knownForbidden = new HashSet<>();

        for (Map.Entry<Request, RequestResult> requestEntry : updated.entrySet()) {
            RequestResult prevResult = original.get(requestEntry.getKey());
            if ((prevResult == null && requestEntry.getValue().permitted) || (prevResult != null && requestEntry.getValue().permitted != prevResult.permitted)) {
                Request request = requestEntry.getKey();

                if (request.known()) {
                    if (requestEntry.getValue().permitted) {
                        knownPermitted.add(request);
                    } else {
                        knownForbidden.add(request);
                    }
                } else {
                    if (requestEntry.getValue().permitted) {
                        unknownPermitted.add(request);
                    } else {
                        unknownForbidden.add(request);
                    }
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
                        knownForbidden.add(request);
                    } else {
                        unknownForbidden.add(request);
                    }
                }
            }
        }

        // Don't return requests that are implied by other ??? requests
        unknownPermitted.forEach(request -> permitted.add(getSummary(request, updated)));
        unknownForbidden.forEach(request -> forbidden.add(getSummary(request, original)));
        knownPermitted.stream()
                .filter(request -> unknownPermitted.stream().noneMatch(unknown -> unknown.subsumes(request)))
                .forEach(request -> permitted.add(getSummary(request, updated)));
        knownForbidden.stream()
                .filter(request -> unknownForbidden.stream().noneMatch(unknown -> unknown.subsumes(request)))
                .forEach(request -> forbidden.add(getSummary(request, original)));

        return new ChangeImpact(permitted, forbidden);
    }

    private static RequestSummary getSummary(Request request, Map<Request, RequestResult> requestMap) {
        return new RequestSummary(request.toHumanReadableString(), requestMap.get(request).policies.stream().map(AstNode::getSourceLoc).toList());
    }
}
