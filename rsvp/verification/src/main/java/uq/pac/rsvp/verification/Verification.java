package uq.pac.rsvp.verification;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.Entity;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.policy.*;
import uq.pac.rsvp.policy.datalog.invariant.InvariantAssignment;
import uq.pac.rsvp.policy.datalog.invariant.InvariantResult;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;
import uq.pac.rsvp.support.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Verification {

    static final boolean TESTING = false;

    private static class RequestResult {
        boolean permitted = false;
        Set<Policy> policies = new HashSet<>();
    }

    /**
     * A request (string with principal, action and resource) and its status (permitted or
     * forbidden)
     */
    public static class RequestStatus {
        private String request;
        private boolean status;

        public String getRequest() {
            return request;
        }

        public boolean isPermitted() {
            return status;
        }

        public RequestStatus(String request, boolean status) {
            this.request = request;
            this.status = status;
        }

        public String toString() {
            return request + " - permitted: " + status;
        }
    }

    /**
     * The results of analysis, including reports and change impact (if performed).
     */
    public static class VerificationResult {
        private Set<Report> reports;
        private Collection<RequestStatus> changeImpact;

        public Set<Report> getReports() {
            return reports;
        }

        /**
         * Get the change impact, in terms of requests and what their (permitted/forbidden) status
         * has changed to.
         */
        public Collection<RequestStatus> getChangeImpact() {
            return changeImpact;
        }

        public VerificationResult(Set<Report> reports, Collection<RequestStatus> changeImpact) {
            this.reports = reports;
            this.changeImpact = changeImpact;
        }
    }

    public static VerificationResult verifyPolicies(Set<List<Path>> policies, Set<Path> schemas, Set<Path> entities, Set<Path> invariants) throws RsvpException, IOException, ConfigurationException, InterruptedException, IllegalAccessException {
        Set<List<Pair<String, Path>>> policyFiles = new HashSet<>();
        Map<String, Path> schemaFiles = new HashMap<>();
        Map<String, Path> entityFiles = new HashMap<>();
        Map<String, Path> invariantFiles = new HashMap<>();

        for (List<Path> versionedPolicy : policies) {
            List<Pair<String, Path>> group = new ArrayList<>();

            for (Path policy : versionedPolicy) {
                group.add(new Pair<>(policy.toString(), policy));
            }

            policyFiles.add(group);
        }

        for (Path schema : schemas) {
            schemaFiles.put(schema.toString(), schema);
        }

        for (Path entitiesFile : entities) {
            entityFiles.put(entities.toString(), entitiesFile);
        }

        for (Path invariantsFile : invariants) {
            invariantFiles.put(invariantsFile.toString(), invariantsFile);
        }

        return verifyPolicies(policyFiles, schemaFiles, entityFiles, invariantFiles);
    }

    public static VerificationResult verifyPolicies(Set<List<Pair<String, Path>>> policies, Map<String, Path> schemas, Map<String, Path> entities, Map<String, Path> invariants) throws RsvpException, IOException, ConfigurationException, InterruptedException, IllegalAccessException {
        if (TESTING) {
            Set<Report> reports = generateRandomReports(policies, schemas, entities, invariants);
            return new VerificationResult(reports, null);
        }

        // TODO: use PolicyParser instead of PolicyProgram and pass file name separately
        if (policies.isEmpty() || policies.iterator().next().isEmpty()) {
            throw new ConfigurationException("No policies provided");
        }

        if (schemas.isEmpty()) {
            throw new ConfigurationException("No schema provided");
        }

        if (entities.isEmpty()) {
            throw new ConfigurationException("No entities provided");
        }

        Set<Report> results = new HashSet<>();

        List<Pair<String, Path>> policyVersions = policies.iterator().next();
        Path policiesPath = policyVersions.getLast().getValue();
        Path prevPoliciesPath = null;
        if (policyVersions.size() >= 2) {
            prevPoliciesPath = policyVersions.get(policyVersions.size() - 2).getValue();
        }
        Path schemaPath = schemas.values().iterator().next();
        Path entitiesPath = entities.values().iterator().next();
        Path invariantsPath = null;
        if (!invariants.isEmpty()) {
            invariantsPath = invariants.values().iterator().next();
        }

        Path dlPath = Files.createTempDirectory("rsvp-");

        Translation translation = new Translation(schemaPath, policiesPath, entitiesPath, invariantsPath, dlPath);

        Map<Policy, RequestSet> policyResults = translation.getPolicyResult();

        // Generate the inverse map, request -> policy set:
        Map<Request, RequestResult> requestPolicyMap = createReverseMapping(policyResults);

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
                Report r = new Report(Severity.Warning, "Policy '" + k.getName() + "' does not " + "match any requests", k.getSourceLoc());
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
                        r = new Report(Severity.Warning, "Policy '" + p.getName() + "' and '" + p2.getName() + "' match the same set of requests", p.getSourceLoc(), p2.getSourceLoc());
                    } else {
                        r = new Report(Severity.Warning, "Policy '" + p.getName() + "' does " + "not match any requests that are not also matched by policy '" + p2.getName() + "'", p.getSourceLoc(), p2.getSourceLoc());
                    }
                    analysisResults.get(p).subsumptionReported = true;
                    results.add(r);
                }
                // TODO if a permit policy is subsumed by a forbid policy, make that clear in the
                //      report
            });
        });

        // Create reports for policies that don't uniquely match any request
        analysisResults.forEach((p, analysisResult) -> {
            if (!analysisResult.uniquelyMatchesRequest && !analysisResult.subsumptionReported) {
                Report r = new Report(Severity.Warning, "Policy '" + p.getName() + "' does not " + "uniquely match any request", "Removing this policy would (by itself) have no effect", p.getSourceLoc());
                results.add(r);
            }
        });

        // Check invariants and report any that don't hold
        Map<Invariant, InvariantResult> invariantResults = translation.getInvariantResult();
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

                Report r = new Report(Severity.Error, "Invariant does not hold", examples, k.getSourceLoc());
                results.add(r);
            }
        });

        List<RequestStatus> changeImpact = null;

        if (prevPoliciesPath != null) {
            // Change impact analysis
            changeImpact = new ArrayList<RequestStatus>();
            Path dlPathPrev = Files.createTempDirectory("rsvp-");

            Translation translationPrev = new Translation(schemaPath, prevPoliciesPath,
                    entitiesPath, invariantsPath, dlPathPrev);
            Map<Policy,RequestSet> prevPolicyResult = translationPrev.getPolicyResult();
            Map<Request, RequestResult> prevRequestResults = createReverseMapping(prevPolicyResult);

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

            for (Entry<Request, RequestResult> requestEntry : requestPolicyMap.entrySet()) {
                RequestResult prevResult = prevRequestResults.get(requestEntry.getKey());
                if (prevResult == null && requestEntry.getValue().permitted || requestEntry.getValue().permitted != prevResult.permitted) {
                    changeImpact.add(new RequestStatus(requestEntry.getKey().getId(),
                            requestEntry.getValue().permitted));
                }
            }

            // Also need to check requests that are permitted previously, but no longer covered:
            for (Entry<Request, RequestResult> requestEntry : prevRequestResults.entrySet()) {
                if (requestEntry.getValue().permitted) {
                    RequestResult currentResult = requestPolicyMap.get(requestEntry.getKey());
                    if (currentResult == null ) {
                        changeImpact.add(new RequestStatus(requestEntry.getKey().getId(), true));
                    }
                }
            }
        }

        return new VerificationResult(results, changeImpact);
    }

    private static Map<Request, RequestResult> createReverseMapping(Map<Policy, RequestSet> policyResults) {
        Map<Request, RequestResult> requestPolicyMap = new HashMap<>();
        policyResults.forEach((k, v) -> {
            v.forEach(r -> {
                RequestResult rr = requestPolicyMap.computeIfAbsent(r, y -> {
                    return new RequestResult();
                });
                if (k.isPermit() && rr.policies.isEmpty()) {
                    rr.permitted = true;
                }
                if (k.isForbid()) {
                    rr.permitted = false;
                }
                rr.policies.add(k);
            });
        });
        return requestPolicyMap;
    }

    public static Set<Report> generateRandomReports(Set<List<Pair<String, Path>>> policies, Map<String, Path> schemas, Map<String, Path> entities, Map<String, Path> invariants) throws IOException, InterruptedException, IllegalAccessException {
        Set<Report> results = new HashSet<>();

        List<PolicyStatement> programStatements = new ArrayList<>();
        Set<Entity> entitySet = new HashSet<>();

        for (List<Pair<String, Path>> policyFile : policies) {
            Pair<String, Path> latest = policyFile.getLast();
            programStatements.addAll(PolicyParser.parse(latest.getKey(), Files.readString(latest.getValue())));
        }

        for (Map.Entry<String, Path> invariantsFile : invariants.entrySet()) {
            programStatements.addAll(PolicyParser.parse(invariantsFile.getKey(), Files.readString(invariantsFile.getValue())));
        }

        RandomReportGenerator randomGenerator = new RandomReportGenerator();

        for (Map.Entry<String, Path> entitiesFile : entities.entrySet()) {
            entitySet.addAll(EntitySet.parse(entitiesFile.getKey(), Files.readString(entitiesFile.getValue())).getEntities());
        }

        RandomReportGenerator.RandomEntityReportGenerator entityReportGenerator = new RandomReportGenerator.RandomEntityReportGenerator(randomGenerator);
        entitySet.forEach(entity -> entityReportGenerator.maybeAddRandomReports(entity, 20));

        results.addAll(entityReportGenerator.reports);

        RandomReportGenerator.RandomPolicyReportGenerator policyReportGenerator = new RandomReportGenerator.RandomPolicyReportGenerator(randomGenerator);

        PolicyProgram policyProgram = PolicyProgram.of(programStatements);
        Collection<Policy> policyAst = policyProgram.getPolicies();
        Collection<Invariant> invariantAst = policyProgram.getInvariants();

        policyAst.forEach(policy -> policy.accept(policyReportGenerator));
        invariantAst.forEach(invariant -> invariant.accept(policyReportGenerator));

        results.addAll(policyReportGenerator.reports);

        Thread.sleep(1000);
        return results;
    }
}
