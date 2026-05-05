package uq.pac.rsvp.verification.policy;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.datalog.invariant.InvariantAssignment;
import uq.pac.rsvp.policy.datalog.invariant.InvariantResult;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.verification.RequestResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PolicyAnalysis {

    private static class PolicyAnalysisResult {
        // Whether the policy uniquely matches at least one request (i.e. there is no other
        // policy that matches the request). For "forbid" policies this considers only other
        // "forbid" policies.
        boolean uniquelyMatchesRequest;

        // Whether it has been reported that this policy is subsumed by another (or if it
        // does not match any requests)
        boolean subsumptionReported;

        // Policies which subsume or match an identical set of requests to this one
        Set<Policy> subsumers;
    }

    public static Set<Report> checkPolicies(Map<Policy, RequestSet> policyResults, Map<Request, RequestResult> requestPolicyMap) {

        Set<Report> reports = new HashSet<>();

        // Create a map of policy to policy-related analysis results:
        Map<Policy, PolicyAnalysisResult> analysisResults = new HashMap<>();
        policyResults.keySet().forEach(p -> {
            analysisResults.put(p, new PolicyAnalysisResult());
        });

        // Check for and report policies not matching any requests:
        policyResults.forEach((k, v) -> {
            if (v.isEmpty()) {
                Report r = new Report(Report.Severity.Warning, "Policy '" + k.getName() + "' does not " + "match any requests", new Report.LocationMessage(k.getSourceLoc(), ""));
                reports.add(r);

                // A policy not matching any requests will be trivially subsumed by every other
                // policy; mark subsumption as reported to avoid further subsumption reports.
                analysisResults.get(k).subsumptionReported = true;
            }
        });

        // Subsumption - which policies may be subsumed (or equal to) others. Start by assuming
        // that all policies have an equal request set, i.e. that each policy may be subsumed by
        // every other individual policy:
        analysisResults.forEach((p, analysisResult) -> {
            if (!analysisResult.subsumptionReported) {
                analysisResult.subsumers = new HashSet<>();
                analysisResult.subsumers.addAll(analysisResults.keySet());
                analysisResult.subsumers.remove(p);
            }
        });

        // Now trim the subsumption sets by processing each request:
        requestPolicyMap.forEach((k, v) -> {
            // The set (v) of policies matching the request has policies which may subsume each
            // other. But, any policy not in the set does not subsume any policy in the set:
            v.policies.forEach(p -> {
                Set<Policy> subsumersOfP = analysisResults.get(p).subsumers;
                subsumersOfP.removeIf(p2 -> !v.policies.contains(p2));
            });

            if (v.policies.size() == 1) {
                analysisResults.get(v.policies.iterator().next()).uniquelyMatchesRequest = true;
            } else {
                // If there is only a single forbid policy that matches, consider it a unique
                // match. (We don't want to report that a forbid policy does not uniquely match
                // requests just because those requests are otherwise permitted).
                Policy singleForbid = null;
                for (Policy p : v.policies) {
                    if (p.isForbid()) {
                        if (singleForbid != null) {
                            singleForbid = null;
                            break;
                        }
                        singleForbid = p;
                    }
                }

                if (singleForbid != null) {
                    analysisResults.get(singleForbid).uniquelyMatchesRequest = true;
                }
            }
        });

        // Create reports for subsumed policies
        analysisResults.forEach((p, analysisResult) -> {
            Set<Policy> ss = analysisResult.subsumers;
            if (ss == null) {
                return;
            }
            ss.forEach(p2 -> {
                // (It is ok for a "forbid" policy to be "subsumed" by a "permit" policy - in that
                // case the forbid acts as a filter over the permit).
                if (!p.isForbid() || p2.isForbid()) {
                    Report r;
                    if (analysisResults.get(p2).subsumers.contains(p)) {
                        // If two policies subsume each other, they match an identical set of
                        // requests (this will be reported twice: once for each of the policies).
                        r = new Report(Report.Severity.Warning, "Policy '" + p.getName() + "' and '" + p2.getName() + "' match the same set of requests", new Report.LocationMessage(p.getSourceLoc(), ""), new Report.LocationMessage(p2.getSourceLoc(), ""));
                    } else {
                        r = new Report(Report.Severity.Warning, "Policy '" + p.getName() + "' does " + "not match any requests that are not also matched by policy '" + p2.getName() + "'", new Report.LocationMessage(p.getSourceLoc(), ""), new Report.LocationMessage(p2.getSourceLoc(), ""));
                    }
                    analysisResults.get(p).subsumptionReported = true;
                    reports.add(r);
                }
                // TODO if a permit policy is subsumed by a forbid policy, make that clear in the
                //      report
            });
        });

        // Create reports for policies that don't uniquely match any request
        analysisResults.forEach((p, analysisResult) -> {
            if (!analysisResult.uniquelyMatchesRequest && !analysisResult.subsumptionReported) {
                Report r = new Report(Report.Severity.Warning, "Policy '" + p.getName() + "' does not " + "uniquely match any request", "Removing this policy would (by itself) have no effect", new Report.LocationMessage(p.getSourceLoc(), ""));
                reports.add(r);
            }
        });

        return reports;
    }

    public static Set<Report> checkInvariants(Map<Invariant, InvariantResult> invariantResults) {
        Set<Report> reports = new HashSet<>();

        // Check invariants and report any that don't hold
        invariantResults.forEach((k, v) -> {
            if (!v.holds()) {
                Set<InvariantAssignment> assignments = v.getAssignments();
                String examples = "";
                if (!assignments.isEmpty()) {
                    if (assignments.size() == 1) {
                        examples = "Counterexample: " + assignments.iterator().next().toString();
                    } else {
                        examples = "Counterexamples: ";
                        Iterator<InvariantAssignment> i = assignments.iterator();
                        for (int count = 0; count < 3 && i.hasNext(); count++) {
                            if (count != 0) {
                                examples += ", ";
                            }
                            examples += i.next().toString();
                        }
                        if (i.hasNext()) {
                            examples += " (and " + (assignments.size() - 3) + " more)";
                        }
                    }
                }

                Report r = new Report(Report.Severity.Error, "Invariant does not hold", examples, new Report.LocationMessage(k.getSourceLoc(), ""));
                reports.add(r);
            }
        });

        return reports;
    }
}
