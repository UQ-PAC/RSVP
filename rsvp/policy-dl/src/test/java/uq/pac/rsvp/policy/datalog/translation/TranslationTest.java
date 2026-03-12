package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.junit.jupiter.api.Assertions.*;

public class TranslationTest {

    private final static Logger logger = new Logger();

    private static class TestInput {
        public final String policyName;
        public final Path policy;
        public final Path schema;
        public final Path entities;
        public final Path datalogDir;
        public final Path testDir;
        public final String testName;

        private TestInput(Path testDir, Path policy, Path schema, Path entities) {
            this.policy = policy;
            this.schema = schema;
            this.entities = entities;
            this.testDir = testDir;
            this.policyName = policy.getFileName().toString()
                    .replaceAll(".cedar$", "");
            this.datalogDir = TestUtil.getDatalogDir(testDir.getFileName().toString(), policyName);
            this.testName = testDir.getFileName().toString();
        }

        public static List<TestInput> load(String dir) {
            Path testDir = Path.of(TranslationTest.class.getClassLoader().getResource(dir).getFile());
            Path schema = Path.of(testDir.toString(), "schema.cedarschema");
            Path entities = Path.of(testDir.toString(), "entities.json");

            assertTrue(Files.exists(testDir) && Files.isDirectory(testDir));
            assertTrue(Files.exists(schema) && Files.isRegularFile(schema));
            assertTrue(Files.exists(entities) && Files.isRegularFile(entities));

            List<TestInput> inputs = new ArrayList<>();
            try (Stream<Path> p = Files.list(testDir)) {
                p.filter(s -> s.toString().endsWith(".cedar")).forEach(policy -> {
                    inputs.add(new TestInput(testDir, policy, schema, entities));
                });
            } catch (IOException e) {
                fail(e);
            }
            return inputs;
        }
    }

    // Sample test for development
    @TestFactory
    Collection<DynamicTest> oneOff() {
        return dynamicTests("github");
    }

    @TestFactory
    Collection<DynamicTest> test() throws IOException {
        List<DynamicTest> tests = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(TestUtil.RESOURCEDIR)) {
            for (Path p : dirs.filter(Files::isDirectory).toList()) {
                tests.addAll(dynamicTests(p.getFileName().toString()));
            }
        }
        return tests;
    }

    Collection<DynamicTest> dynamicTests(String name) {
        Collection<DynamicTest> tests = new ArrayList<>();
        TestInput.load(name).forEach(t -> {
            tests.add(DynamicTest.dynamicTest(t.testName + "-" + t.policyName, () -> differentialTest(t)));
        });
        return tests;
    }

    /**
     * Differential test for Cedar and RSVP.
     * The test runs both, RSVP and Cedar authorisation engines and compares the results that should agree
     */
    void differentialTest(TestInput test) throws IOException, AuthException, InterruptedException, RsvpException {
        logger.info(YELLOW, "Policy: " + test.policy)
                .info(CYAN, Files.readString(test.policy))
                .info(MAGENTA, "Datalog directory: " + test.datalogDir);

        RequestAuth rsvpAuth = RequestAuth.load(test.schema, test.policy, test.entities, test.datalogDir);
        assertTrue(Collections.disjoint(rsvpAuth.getForbiddenRequests(), rsvpAuth.getPermittedRequests()));

        AuthorizationEngine cedarAuth = new BasicAuthorizationEngine();
        Entities cedarEntities = Entities.parse(test.entities);

        Schema cedarSchema = Schema.parse(Schema.JsonOrCedar.Cedar, Files.readString(test.schema));
        PolicySet cedarPolicies = PolicySet.parsePolicies(test.policy);

        int [] rsvpRequestCounter = new int [2];
        int [] cedarRequestCounter = new int [2];
        for (Request rsvpRequest : rsvpAuth.getActionableRequests()) {
            RequestAuth.Decision rsvpDecision = rsvpAuth.authorize(rsvpRequest);
            assertTrue(rsvpDecision == RequestAuth.Decision.Deny ||
                    rsvpDecision == RequestAuth.Decision.Allow);

            AuthorizationRequest cedarRequest = rsvpRequest.getCedarRequest(cedarSchema);
            AuthorizationResponse cedarResponse =
                    cedarAuth.isAuthorized(cedarRequest, cedarPolicies, cedarEntities);
            assertEquals(AuthorizationResponse.SuccessOrFailure.Success, cedarResponse.type,
                    () -> cedarResponse.errors.orElseThrow(AssertionError::new).toString());
            AuthorizationSuccessResponse cedarSuccess =
                    cedarResponse.success.orElseThrow(AssertionError::new);
            AuthorizationSuccessResponse.Decision cedarDecision = cedarSuccess.getDecision();


            if (cedarDecision == AuthorizationSuccessResponse.Decision.Allow &&
                    rsvpDecision == RequestAuth.Decision.Allow) {
                logger.info(GREEN, rsvpRequest + ":  " + rsvpDecision + "/" + cedarDecision);
            } else if (cedarDecision == AuthorizationSuccessResponse.Decision.Deny &&
                    rsvpDecision == RequestAuth.Decision.Deny) {
                logger.info(BLUE, rsvpRequest + ":  " + rsvpDecision + "/" + cedarDecision);
            } else {
                logger.error(rsvpRequest + ":  " + rsvpDecision + "/" + cedarDecision);
            }
            cedarRequestCounter[cedarDecision.ordinal()]++;
            rsvpRequestCounter[rsvpDecision.ordinal()]++;
        }
        logger.attr(Ansi.Attribute.INTENSITY_BOLD)
                .info(GREEN, "RSVP Requests (allow/deny): %d/%d\n",
                rsvpAuth.getActionableRequests().size(), rsvpRequestCounter[0], rsvpRequestCounter[1]);
        logger.attr(Ansi.Attribute.INTENSITY_BOLD)
                .info(BLUE, "Cedar Requests (allow/deny): %d/%d\n",
                rsvpAuth.getActionableRequests().size(), cedarRequestCounter[0], cedarRequestCounter[1]);
        assertArrayEquals(rsvpRequestCounter, cedarRequestCounter, "Mismatched request decisions found");
    }
}
