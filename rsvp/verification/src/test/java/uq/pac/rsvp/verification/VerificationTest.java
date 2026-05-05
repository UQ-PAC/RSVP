package uq.pac.rsvp.verification;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.FileSet;
import uq.pac.rsvp.support.reporting.Report;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerificationTest {

    @Test
    void testVerification() throws Exception {
        String projectRoot = System.getProperty("test.rootdir");
        Path testResourcesDir = Path.of(projectRoot, "src", "test", "resources");
        Path childrenClinic1 = testResourcesDir.resolve("childrenclinic").resolve("1");

        Path entities = childrenClinic1.resolve("childrenclinic-rsvp-entities.json");
        Path policy = childrenClinic1.resolve("childrenclinic-rsvp-policy.cedar");
        Path schema = childrenClinic1.resolve("childrenclinic-rsvp-schema.cedarschema");

        FileSet fileset = new FileSet().addEntities(entities).addPolicies(policy).addSchema(schema);

        VerificationResult result = Verification.verifyPolicies(fileset);

        assertTrue(result.reports().isEmpty());
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

        FileSet fileset = new FileSet().addEntities(entities).addPolicies(policy).addSchema(schema);

        VerificationResult result = Verification.verifyPolicies(fileset);

        assertEquals(2, result.reports().size());

        Set<String> expectedMsgs = new HashSet<>(List.of(
                "Policy 'policy8' does not match any requests that are not also matched by policy 'policy6'",
                "Policy 'policy14' does not match any requests that are not also matched by policy 'policy6'"
        ));

        for (Report report : result.reports()) {
            assertTrue(expectedMsgs.remove(report.getMessage()));
        }
    }

}
