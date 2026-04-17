package uq.pac.rsvp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicyFileEntry;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.ConditionalExpression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.RecordExpression;
import uq.pac.rsvp.policy.ast.expr.SetExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitorImpl;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;

public class Verification {

    public static Set<Report> verifyPolicies(String policyFilename, Path policiesPath, Path schemaPath, Path entities)
            throws RsvpException, IOException {

        Set<Report> results = new HashSet<>();

        Path dlPath = Files.createTempDirectory("rsvp-");

        Schema schema = Schema.parseCedarSchema(schemaPath);
        PolicySet policies = PolicySet.parseCedarPolicySet(policyFilename, Files.readString(policiesPath));

        Translation translation =
                new Translation(schema, policies, entities, null, dlPath);

        Map<Policy, RequestSet> policyResults = translation.getPolicyResult();

        // Generate the inverse map, request -> policy set:
        Map<Request, Set<Policy>> requestPolicyMap = new HashMap<>();
        policyResults.forEach((k, v) -> {
            v.forEach(r -> {
                requestPolicyMap.computeIfAbsent(r, y -> { return new HashSet<>(); }).add(k);
            });
        });

        class PolicyAnalysisResult {
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

        // Create a map of policy to policy-related analysis results:
        Map<Policy, PolicyAnalysisResult> analysisResults = new HashMap<>();
        policyResults.keySet().forEach(p -> {
            analysisResults.put(p, new PolicyAnalysisResult());
        });

        // Check for and report policies not matching any requests:
        policyResults.forEach((k, v) -> {
            if (v.isEmpty()) {
                Report r = new Report(Severity.Warning, "Policy '" + k.getName() + "' does not "
                        + "match any requests",
                        k.getSourceLoc());
                results.add(r);

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
            v.forEach(p -> {
               Set<Policy> subsumersOfP = analysisResults.get(p).subsumers;
               subsumersOfP.removeIf(p2 -> !v.contains(p2));
            });

            if (v.size() == 1) {
                analysisResults.get(v.iterator().next()).uniquelyMatchesRequest = true;
            } else {
                // If there is only a single forbid policy that matches, consider it a unique
                // match. (We don't want to report that a forbid policy does not uniquely match
                // requests just because those requests are otherwise permitted).
                Policy singleForbid = null;
                for (Policy p : v) {
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
                        r = new Report(Severity.Warning, "Policy '" + p.getName() + "' and '"
                                + p2.getName() + "' match the same set of requests",
                                p.getSourceLoc(), p2.getSourceLoc());
                    }
                    else {
                        r = new Report(Severity.Warning, "Policy '" + p.getName() + "' does "
                                + "not match any requests that are not also matched by policy '"
                                + p2.getName() + "'",
                                p.getSourceLoc(), p2.getSourceLoc());
                    }
                    analysisResults.get(p).subsumptionReported = true;
                    results.add(r);
                }
                // TODO if a permit policy is subsumed by a forbid policy, make that clear in the
                // report
            });
        });

        // Create reports for policies that don't uniquely match any request
        analysisResults.forEach((p, analysisResult) -> {
            if (!analysisResult.uniquelyMatchesRequest && !analysisResult.subsumptionReported) {
                Report r = new Report(Severity.Warning, "Policy '" + p.getName() + "' does not "
                        + "uniquely match any request",
                        "Removing this policy would (by itself) have no effect",
                        p.getSourceLoc());
                results.add(r);
            }
        });

        return results;
    }

    public static Set<Report> verify(PolicySet policies, Schema schema) {
        RandomReportGenerator generator = new RandomReportGenerator();

        policies.accept(generator);

        return generator.reports;
    }

    private static class RandomReportGenerator extends PolicyVisitorImpl {

        public final Set<Report> reports;

        private final Random random;

        RandomReportGenerator() {
            reports = new HashSet<>();
            random = new Random();
        }

        @Override
        public void visitPolicy(Policy policy) {
            maybeAddRandomReport(policy, 20);
            super.visitPolicy(policy);
        }

        @Override
        public void visitBinaryExpr(BinaryExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitBinaryExpr(expr);
        }

        @Override
        public void visitCallExpr(CallExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitCallExpr(expr);
        }

        @Override
        public void visitConditionalExpr(ConditionalExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitConditionalExpr(expr);
        }

        @Override
        public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitPropertyAccessExpr(expr);
        }

        @Override
        public void visitRecordExpr(RecordExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitRecordExpr(expr);
        }

        @Override
        public void visitSetExpr(SetExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitSetExpr(expr);
        }

        @Override
        public void visitUnaryExpr(UnaryExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitUnaryExpr(expr);
        }

        private void maybeAddRandomReport(PolicyFileEntry entry, int probability) {
            int p = random.nextInt(100);
            SourceLoc loc = entry.getSourceLoc();
            if (p <= probability && loc != SourceLoc.MISSING) {
                reports.add(generateRandomReport(loc));
            }
        }

        private Report generateRandomReport(SourceLoc loc) {
            int s = random.nextInt(100);
            int m = random.nextInt(100);
            int d = random.nextInt(100);

            Severity severity = s < 34 ? Severity.Info : s < 67 ? Severity.Warning : Severity.Error;
            String message = m < 34 ? "Fantastic. Great move. Well done Angus."
                    : m < 67 ? "Ugly implementation" : "Who thought this was a good idea?";
            String detail = d < 50 ? "" : "This is a very detailed report. "
                            + "Look at all of the details that are included here. "
                            + "So many details that need to be included in the report so that you can fully understand it.";

            return new Report(severity, message, detail, loc);
        }

    }

}
