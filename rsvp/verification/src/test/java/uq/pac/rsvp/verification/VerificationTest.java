package uq.pac.rsvp.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.verification.Verification.VerificationResult;

public class VerificationTest {

    @Test
    void testVerification() throws Exception {
        String projectRoot = System.getProperty("test.rootdir");
        Path testResourcesDir = Path.of(projectRoot, "src", "test", "resources");
        Path childrenClinic1 = testResourcesDir.resolve("childrenclinic").resolve("1");

        Path entities = childrenClinic1.resolve("childrenclinic-rsvp-entities.json");
        Path policy = childrenClinic1.resolve("childrenclinic-rsvp-policy.cedar");
        Path schema = childrenClinic1.resolve("childrenclinic-rsvp-schema.cedarschema");

        VerificationResult result = Verification.verifyPolicies(Set.of(List.of(policy)),
                Set.of(schema), Set.of(entities), Collections.emptySet());

        assertTrue(result.getReports().isEmpty());
    }

    @Test
    void testVerificationWithReport() throws Exception {
        String projectRoot = System.getProperty("test.rootdir");
        Path testResourcesDir = Path.of(projectRoot, "src", "test", "resources");
        Path childrenClinic1 = testResourcesDir.resolve("childrenclinic").resolve("1");
        Path childrenClinic2 = testResourcesDir.resolve("childrenclinic").resolve("2");

        // Take 2nd version of policy (mistake in policy #6)
        Path entities = childrenClinic1.resolve("childrenclinic-rsvp-entities.json");
        Path policy = childrenClinic2.resolve("childrenclinic-rsvp-policy.cedar");
        Path schema = childrenClinic1.resolve("childrenclinic-rsvp-schema.cedarschema");

        VerificationResult result = Verification.verifyPolicies(Set.of(List.of(policy)),
                Set.of(schema), Set.of(entities), Collections.emptySet());

        assertEquals(2, result.getReports().size());

        Set<String> expectedMsgs = new HashSet<>();
        expectedMsgs.addAll(List.of(
                "Policy 'policy8' does not match any requests that are not also matched by policy 'policy6'",
                "Policy 'policy14' does not match any requests that are not also matched by policy 'policy6'"
                ));

        for (Report report : result.getReports()) {
            assertTrue(expectedMsgs.remove(report.getMessage()));
        }
    }

}
