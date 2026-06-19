package uq.pac.rsvp.verification;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.support.reporting.Report;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        // Single coverage report
        assertEquals(1, result.reports().size());
        assertEquals(Report.Severity.Info, result.reports().iterator().next().getSeverity());
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

        Set<Report> reports = Verification.verifyPolicies(fileset).reports();

        assertEquals(3, reports.size());

        int subsumedCount = 0;
        int noCoverageCount = 0;
        for (Report report : reports) {
            if (report.getMessage().equals("Subsumed Policy")) {
                subsumedCount++;
            }
            else if (report.getMessage().equals("Not all requests are covered by policy")) {
                noCoverageCount++;
            }
        }

        assertEquals(2, subsumedCount);
        assertEquals(1, noCoverageCount);
    }

}
