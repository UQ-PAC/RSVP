package uq.pac.rsvp.verification;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.FileSet;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;
import uq.pac.rsvp.verification.impact.ImpactAnalysis;
import uq.pac.rsvp.verification.impact.RequestStatus;
import uq.pac.rsvp.verification.policy.PolicyAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

        fileset.loadFiles();

        Set<Report> reports = new HashSet<>();
        VerificationCache cache = new VerificationCache(fileset);

        try {
            Translation translation = translate(fileset);
            Map<Policy, RequestSet> policyResults = translation.getPolicyResult();

            // Generate the inverse map, request -> policy set:
            Map<Request, RequestResult> requestPolicyMap = createReverseMapping(policyResults);
            cache.cacheMapping(FileSet.LATEST, requestPolicyMap);

            reports.addAll(PolicyAnalysis.checkPolicies(policyResults, requestPolicyMap));
            reports.addAll(PolicyAnalysis.checkInvariants(translation.getInvariantResult()));
        }
        catch (TranslationError translationError) {
            reports.add(new Report(Severity.Error, "Translation error", translationError.getMessage()));
        }

        return new VerificationResult(reports, cache);
    }

    public static List<RequestStatus> getImpact(String original, String updated, VerificationCache cache) throws IOException, InterruptedException {
        List<RequestStatus> changeImpact = cache.getImpact(original, updated);

        if (changeImpact == null) {

            Map<Request, RequestResult> originalMapping = getOrCacheMapping(original, cache);
            Map<Request, RequestResult> updatedMapping = getOrCacheMapping(updated, cache);

            changeImpact = ImpactAnalysis.computeImpact(originalMapping, updatedMapping);
            cache.cacheImpact(original, updated, changeImpact);
        }

        if (TESTING) {
            Thread.sleep(1000);
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
        return new Translation(fileset, dlPath, policyVersion);
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

}
