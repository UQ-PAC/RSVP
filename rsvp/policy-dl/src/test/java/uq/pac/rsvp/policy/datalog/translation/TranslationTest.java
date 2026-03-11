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
import org.fusesource.jansi.AnsiColors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
        public final Path permitted;
        public final Path forbidden;

        private TestInput(Path testDir, Path policy, Path schema, Path entities) {
            this.policy = policy;
            this.schema = schema;
            this.entities = entities;
            this.testDir = testDir;
            this.policyName = policy.getFileName().toString()
                    .replaceAll(".cedar$", "");
            this.datalogDir = TestUtil.getDatalogDir(testDir.getFileName().toString(), policyName);
            this.testName = testDir.getFileName().toString();
            this.permitted = Path.of(testDir.toString(), policyName + ".allow");
            this.forbidden = Path.of(testDir.toString(), policyName + ".deny");
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
    @Disabled
    Collection<DynamicTest> oneOff() {
        return dynamicTests("in-ns");
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
            tests.add(DynamicTest.dynamicTest("func-" + t.testName + "-" + t.policyName, () -> functionalTest(t)));
            tests.add(DynamicTest.dynamicTest("diff-" + t.testName + "-" + t.policyName, () -> differentialTest(t)));
        });
        return tests;
    }

    void functionalTest(TestInput test) throws AuthException, IOException, InterruptedException, RsvpException {
        RequestAuth rsvpAuth = RequestAuth.load(test.schema, test.policy, test.entities, test.datalogDir);
        logger.info(YELLOW, "Policy: " + test.policy)
                .info(CYAN, Files.readString(test.policy))
                .info(MAGENTA, "Datalog directory: " + test.datalogDir);

        BiConsumer<Supplier<Set<Request>>, Path> doTest = (req, oracle) -> {
            try {
                // Get requests from auth
                String str = req.get().stream()
                        .sorted(Comparator.comparing(Request::getId))
                        .map(Request::getId)
                        .collect(Collectors.joining("\n"));
                if (TestUtil.GENERATE_ORACLES) {
                    Path path = Path.of(TestUtil.RESOURCEDIR.toString(),
                            test.testDir.getFileName().toString(),
                            oracle.getFileName().toString());
                    Files.writeString(path, str);
                } else {
                    String oracleStr = Files.readString(oracle);
                    assertEquals(oracleStr, str);
                }
            } catch (IOException e) {
                fail(e);
            }
        };

        doTest.accept(rsvpAuth::getPermittedRequests, test.permitted);
        doTest.accept(rsvpAuth::getForbiddenRequests, test.forbidden);
    }

    /**
     * Differential test for Cedar and RSVP.
     * The test runs both, RSVP and Cedar authorisation engines and compares the results that should agree
     */
    void differentialTest(TestInput test) throws IOException, AuthException, InterruptedException, RsvpException {
        RequestAuth rsvpAuth = RequestAuth.load(test.schema, test.policy, test.entities, test.datalogDir);
        assertTrue(Collections.disjoint(rsvpAuth.getForbiddenRequests(), rsvpAuth.getPermittedRequests()));

        logger.info(YELLOW, "Policy: " + test.policy)
            .info(CYAN, Files.readString(test.policy))
            .info(MAGENTA, "Datalog directory: " + test.datalogDir);

        AuthorizationEngine cedarAuth = new BasicAuthorizationEngine();
        Entities cedarEntities = Entities.parse(test.entities);

        Schema cedarSchema = Schema.parse(Schema.JsonOrCedar.Cedar, Files.readString(test.schema));
        PolicySet cedarPolicies = PolicySet.parsePolicies(test.policy);

        int [] requestCounter = new int [3];
        for (Request rsvpRequest : rsvpAuth.getActionableRequests()) {
            RequestAuth.Result rsvpDecision = rsvpAuth.authorize(rsvpRequest);
            assertTrue(rsvpDecision == RequestAuth.Result.Deny ||
                    rsvpDecision == RequestAuth.Result.Allow);

            AuthorizationRequest cedarRequest = rsvpRequest.getCedarRequest(cedarSchema);
            AuthorizationResponse cedarResponse =
                    cedarAuth.isAuthorized(cedarRequest, cedarPolicies, cedarEntities);
            assertEquals(AuthorizationResponse.SuccessOrFailure.Success, cedarResponse.type,
                    () -> cedarResponse.errors.orElseThrow(AssertionError::new).toString());
            AuthorizationSuccessResponse cedarSuccess =
                    cedarResponse.success.orElseThrow(AssertionError::new);
            AuthorizationSuccessResponse.Decision cedarDecision = cedarSuccess.getDecision();

            if (cedarDecision == AuthorizationSuccessResponse.Decision.Allow &&
                    rsvpDecision == RequestAuth.Result.Allow) {
                requestCounter[0]++;
                logger.info(GREEN, rsvpRequest + ":  " + rsvpDecision + "/" + cedarDecision);
            } else if (cedarDecision == AuthorizationSuccessResponse.Decision.Deny &&
                    rsvpDecision == RequestAuth.Result.Deny) {
                requestCounter[1]++;
                logger.info(BLUE, rsvpRequest + ":  " + rsvpDecision + "/" + cedarDecision);
            } else {
                logger.error(""" 
                        Request mismatch:
                            RSVP: %s,
                            Cedar: %s
                        RSVP request: %s
                        CedarRequest: %s
                        """.formatted(rsvpDecision, cedarDecision, rsvpRequest, cedarRequest));
                requestCounter[2]++;
            }
        }
        logger.info(GREEN, "Validated Requests (allow/deny/failed): %d (%d/%d/%d)\n",
                rsvpAuth.getActionableRequests().size(), requestCounter[0], requestCounter[1], requestCounter[2]);
        assertEquals(0, requestCounter[2], "%d mismatched requests".formatted(requestCounter[2]));
    }
}
