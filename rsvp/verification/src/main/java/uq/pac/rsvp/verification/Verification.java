package uq.pac.rsvp.verification;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.support.error.LocationError;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;
import uq.pac.rsvp.verification.impact.ChangeImpact;
import uq.pac.rsvp.verification.impact.ImpactAnalysis;
import uq.pac.rsvp.verification.policy.PolicyAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Verification {

    static final boolean TESTING = false;

    public static VerificationResult verifyPolicies(FileSet fileset) throws RsvpException, IOException, ConfigurationException, InterruptedException, IllegalAccessException {

        if (TESTING) {
            Set<Report> reports = RandomReportGenerator.generateRandomReports(fileset);
            Thread.sleep(1000);
            return new VerificationResult(reports, null);
        }

        if (fileset.noPolicies()) {
            throw new ConfigurationException("No policies provided");
        }

        if (fileset.noSchemas()) {
            throw new ConfigurationException("No schema provided");
        }

        if (fileset.noEntities()) {
            throw new ConfigurationException("No entities provided");
        }

        Set<Report> reports = new HashSet<>();
        VerificationCache cache = null;

        try {
            fileset.loadFiles();
            cache = new VerificationCache(fileset);
            Translation translation = translate(fileset);
            Map<Policy, RequestSet> policyResults = translation.getPolicyResult();
            RequestSet allRequests = translation.getActionableRequests();

            // Generate the inverse map, request -> policy set:
            Map<Request, RequestResult> requestPolicyMap = createReverseMapping(policyResults);
            cache.cacheMapping(FileSet.LATEST, requestPolicyMap);

            reports.addAll(PolicyAnalysis.checkPolicies(allRequests, policyResults, requestPolicyMap));
            reports.addAll(PolicyAnalysis.checkInvariants(translation.getInvariantResult()));
        } catch (LocationError e) {
            // User-error, such as syntax or validation error
            reports.add(new Report(Severity.Error, e.getTitle() + " Error", e.getMessage(), e.getLocation()));
        }

        return new VerificationResult(reports, cache);
    }

    public static ChangeImpact getImpact(String original, String updated, VerificationCache cache) throws IOException, InterruptedException {
        ChangeImpact changeImpact = cache.getImpact(original, updated);

        if (changeImpact == null) {

            Map<Request, RequestResult> originalMapping = getOrCacheMapping(original, cache);
            Map<Request, RequestResult> updatedMapping = getOrCacheMapping(updated, cache);

            changeImpact = ImpactAnalysis.computeImpact(originalMapping, updatedMapping);
            cache.cacheImpact(original, updated, changeImpact);
        }

        return changeImpact;
    }

    private static Map<Request, RequestResult> getOrCacheMapping(String version, VerificationCache cache) throws IOException {
        Map<Request, RequestResult> mapping = cache.getMapping(version);

        if (mapping == null) {
            Translation translation = translate(cache.getFileset(), version);
            Map<Policy, RequestSet> policyResults = translation.getPolicyResult();

            mapping = createReverseMapping(policyResults);
            cache.cacheMapping(version, mapping);
        }

        return mapping;
    }


    private static Translation translate(FileSet fileset) throws IOException {
        return translate(fileset, FileSet.LATEST);
    }

    private static Translation translate(FileSet fileset, String policyVersion) throws IOException {
        Path dlPath = Files.createTempDirectory("rsvp-");
        Schema schema = Schema.of(fileset.getSchemaStatements());
        PolicyProgram program = fileset.getPolicyProgram(policyVersion);
        EntitySet entities = new EntitySet(fileset.getEntities());
        return new Translation(schema, program, entities, dlPath);
    }

    private static Map<Request, RequestResult> createReverseMapping(Map<Policy, RequestSet> policyResults) {
        Map<Request, RequestResult> requestPolicyMap = new HashMap<>();
        policyResults.forEach((k, v) -> {
            v.forEach(r -> {
                RequestResult rr = requestPolicyMap.computeIfAbsent(r, y -> new RequestResult());
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

}
