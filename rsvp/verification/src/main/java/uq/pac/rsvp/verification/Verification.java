package uq.pac.rsvp.verification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicyProgram;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.ConditionalExpression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.RecordExpression;
import uq.pac.rsvp.policy.ast.expr.SetExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitorImpl;
import uq.pac.rsvp.policy.datalog.invariant.InvariantAssignment;
import uq.pac.rsvp.policy.datalog.invariant.InvariantResult;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.support.util.Pair;

public class Verification {


    // FIXME: source loc should be correct from outset
    private static SourceLoc mapSourceLoc(String filename, SourceLoc original) {
        return new SourceLoc(filename, original.offset, original.len, original.getStartLoc(),
                original.getEndLoc());
    }

    public static Set<Report> verifyPolicies(Set<List<Pair<String, Path>>> policies, Set<Path> schemas, Set<Path> entities, Set<Pair<String, Path>> invariants)
            throws RsvpException, IOException, ConfigurationException, InterruptedException {

        Set<Report> results = new HashSet<>();

        if (policies.isEmpty() || policies.iterator().next().isEmpty()) {
            throw new ConfigurationException("No policies provided");
        }

        if (schemas.isEmpty()) {
            throw new ConfigurationException("No schema provided");
        }

        if (entities.isEmpty()) {
            throw new ConfigurationException("No entities provided");
        }

        Pair<String, Path> policyFile = policies.iterator().next().get(0);
        String policyFilename = policyFile.getKey();
        Path policiesPath = policyFile.getValue();
        Path schemaPath = schemas.iterator().next();
        Path entitiesPath = entities.iterator().next();
        Pair<String, Path> invariantsFile = invariants.iterator().next();
        String invariantsFilename = invariantsFile.getKey();
        Path invariantsPath = invariantsFile.getValue();

        Path dlPath = Files.createTempDirectory("rsvp-");

        // Schema schema = Schema.parseCedarSchema(schemaPath);
        // PolicySet policySet = PolicySet.parseCedarPolicySet(policyFilename,
        //         Files.readString(policiesPath));

        Translation translation = new Translation(schemaPath, policiesPath, entitiesPath, invariantsPath, dlPath);

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

        // Check invariants and report any that don't hold
        Map<Invariant,InvariantResult> invariantResults = translation.getInvariantResult();
        invariantResults.forEach((k, v) -> {
            if (!v.holds()) {
                Set<InvariantAssignment> assignments = v.getAssignments();
                String examples = "";
                if (!assignments.isEmpty()) {
                    if (assignments.size() == 1) {
                        examples = "Counterexample: " + assignments.iterator().next().toString();
                    }
                    else {
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

                SourceLoc newLoc = mapSourceLoc(invariantsFilename, k.getSourceLoc());

                Report r = new Report(Severity.Error, "Invariant does not hold", examples, newLoc);
                results.add(r);
            }
        });

        // TODO: remove
        // results.addAll(generateRandomReports(policies));

        return results;
    }


    public static Set<Report> generateRandomReports(Set<List<Pair<String, Path>>> policies)
            throws RsvpException, IOException, ConfigurationException, InterruptedException {
        Set<Report> results = new HashSet<>();

        for (List<Pair<String, Path>> policyFile : policies) {
            Pair<String, Path> recent = policyFile.get(policyFile.size() - 1);
            String policyFilename = recent.getKey();
            Path policiesPath = recent.getValue();

            RandomReportGenerator generator = new RandomReportGenerator(policyFilename);

            PolicyProgram policyProgram = PolicyProgram.parse(policiesPath);
            Collection<Policy> parsed = policyProgram.getPolicies();

            parsed.forEach(policy -> policy.accept(generator));

            results.addAll(generator.reports);
        }

        Thread.sleep(1000);
        return results;
    }

    private static class RandomReportGenerator extends PolicyVisitorImpl {

        public final Set<Report> reports;

        private final Random random;
        private final String filename;

        RandomReportGenerator(String filename) {
            reports = new HashSet<>();
            random = new Random();
            this.filename = filename;
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

        private void maybeAddRandomReport(AstNode entry, int probability) {
            int p = random.nextInt(100);
            SourceLoc loc = mapSourceLoc(this.filename, entry.getSourceLoc());
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
            String detail = d < 50 ? ""
                    : "This is a very detailed report. "
                            + "Look at all of the details that are included here. "
                            + "So many details that need to be included in the report so that you can fully understand it.";

            return new Report(severity, message, detail, loc);
        }

    }

}
