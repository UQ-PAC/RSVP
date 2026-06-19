package uq.pac.rsvp.verification.policy;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.datalog.translation.InvariantAssignment;
import uq.pac.rsvp.policy.datalog.translation.InvariantResult;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;
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

    public static Set<Report> checkPolicies(RequestSet allRequests,
            Map<Policy, RequestSet> policyResults, Map<Request, RequestResult> requestPolicyMap) {

        Set<Report> reports = new HashSet<>();

        // Create a map of policy to policy-related analysis results:
        Map<Policy, PolicyAnalysisResult> analysisResults = new HashMap<>();
        policyResults.keySet().forEach(p -> {
            analysisResults.put(p, new PolicyAnalysisResult());
        });

        // Check for and report policies not matching any requests:
        policyResults.forEach((policy, v) -> {
            if (v.isEmpty()) {
                reports.add(new PolicyReport.UnusedPolicy(policy));

                // A policy not matching any requests will be trivially subsumed by every other
                // policy; mark subsumption as reported to avoid further subsumption reports.
                analysisResults.get(policy).subsumptionReported = true;
            }
        });

        // Subsumption - which policies may be subsumed (or equal to) others. Start by assuming
        // that all policies have an equal request set, i.e. that each policy may be subsumed by
        // every other individual policy:
        analysisResults.forEach((policy, analysisResult) -> {
            if (!analysisResult.subsumptionReported) {
                analysisResult.subsumers = new HashSet<>();
                analysisResult.subsumers.addAll(analysisResults.keySet());
                analysisResult.subsumers.remove(policy);
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
                    if (analysisResults.get(p2).subsumers.contains(p)) {
                        // If two policies subsume each other, they match an identical set of
                        // requests.
                        if (!analysisResults.get(p2).subsumptionReported) {
                            // If one policy is forbid and the other permit, we have contradictory
                            // rather than identical policies. The check above ("if (!p.isForbid()...")
                            // ensures that if this is the case, p2 is forbid and p is permit:
                            if (p2.isForbid() && p.isPermit()) {
                                reports.add(new PolicyReport.ContradictoryPolicies(p, p2, true));
                            } else {
                                reports.add(new PolicyReport.IdenticalPolicies(p, p2));
                            }
                        }
                    } else {
                        // As above, if the permission status mismatches, report as contradictory
                        // rather than subsumed.
                        if (p2.isForbid() && p.isPermit()) {
                            reports.add(new PolicyReport.ContradictoryPolicies(p, p2, false));
                        } else {
                            reports.add(new PolicyReport.SubsumedPolicy(p, p2));
                        }
                    }
                    analysisResults.get(p).subsumptionReported = true;
                }
            });
        });

        // Create reports for policies that don't uniquely match any request
        analysisResults.forEach((p, analysisResult) -> {
            if (!analysisResult.uniquelyMatchesRequest && !analysisResult.subsumptionReported) {
                reports.add(new PolicyReport.SubsumedPolicy(p));
            }
        });

        // Determine request coverage; report (as information) if coverage isn't complete
        Set<Request> uncoveredRequests = new HashSet<>();
        for (Request r : allRequests.getRequests()) {
            if (!requestPolicyMap.containsKey(r)) {
                uncoveredRequests.add(r);
            }
        }
        if (!uncoveredRequests.isEmpty()) {
            reports.add(new Report(Severity.Info, "Not all requests are covered by policy",
                    "" + uncoveredRequests.size() + " out of the " + allRequests.size()
                    + " possible requests are not matched by any policy. \nExample:" + uncoveredRequests.iterator().next().toHumanReadableString()));
        }

        return reports;
    }

    static final int MAX_COUNTEREXAMPLES = 5;

    public static Set<Report> checkInvariants(Map<Invariant, InvariantResult> invariantResults) {
        Set<Report> reports = new HashSet<>();

        // Check invariants and report any that don't hold
        invariantResults.forEach((k, v) -> {
            if (!v.holds()) {
                Set<InvariantAssignment> assignments = v.getAssignments();
                StringBuilder counterExamples = new StringBuilder();
                if (!assignments.isEmpty()) {
                    if (assignments.size() == 1) {
                        counterExamples.append(assignments.iterator().next().toHumanReadableString());
                    } else {
                        Iterator<InvariantAssignment> i = assignments.iterator();
                        for (int count = 0; count < MAX_COUNTEREXAMPLES && i.hasNext(); count++) {
                            if (count > 0) {
                                counterExamples.append("\n");
                            }
                            counterExamples.append(i.next().toHumanReadableString());

                            if (count < assignments.size() - 1) {
                                counterExamples.append(",");
                            }
                        }
                        if (i.hasNext()) {
                            counterExamples.append("\nAnd ");
                            counterExamples.append(assignments.size() - MAX_COUNTEREXAMPLES);
                            counterExamples.append(" more counter-example(s)...");
                        }
                    }
                }

                reports.add(new PolicyReport.InvariantNotHeld(k, counterExamples.toString()));
            }
        });

        return reports;
    }
}
