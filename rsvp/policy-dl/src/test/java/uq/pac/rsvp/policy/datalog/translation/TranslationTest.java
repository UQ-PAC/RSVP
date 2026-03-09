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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.ITALIC;
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
            this.permitted = Path.of(testDir.toString(), policyName + ".permitted");
            this.forbidden = Path.of(testDir.toString(), policyName + ".forbidden");
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

    @ParameterizedTest
    @ValueSource(strings = {
        "ancestors",
        "photoapp"
    })
    void test(String name) throws AuthException, IOException, InterruptedException {
        functionalTest(name);
        differentialTest(name);
    }

    void functionalTest(String name) throws AuthException, IOException, InterruptedException {
        logger.attr(INTENSITY_BOLD).attr(ITALIC)
                .info(CYAN, " ======= Functional Test: %s ======= ", name);

        for (TestInput test : TestInput.load(name)) {
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
                        Path path = Path.of(TestUtil.TESTRESOURCEDIR.toString(),
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
    }

    /**
     * Differential test for Cedar and RSVP.
     * The test runs both, RSVP and Cedar authorisation engines and compares the results that should agree
     */
    void differentialTest(String name) throws IOException, AuthException, InterruptedException {
        logger.attr(INTENSITY_BOLD).attr(ITALIC)
                .info(CYAN, " ======= Differential Test: %s ======= ", name);
        for (TestInput test : TestInput.load(name)) {
            RequestAuth rsvpAuth = RequestAuth.load(test.schema, test.policy, test.entities, test.datalogDir);
            assertTrue(Collections.disjoint(rsvpAuth.getForbiddenRequests(), rsvpAuth.getPermittedRequests()));

            logger.info(YELLOW, "Policy: " + test.policy)
                .info(CYAN, Files.readString(test.policy))
                .info(MAGENTA, "Datalog directory: " + test.datalogDir);

            AuthorizationEngine cedarAuth = new BasicAuthorizationEngine();
            Entities cedarEntities = Entities.parse(test.entities);

            Schema cedarSchema = Schema.parse(Schema.JsonOrCedar.Cedar, Files.readString(test.schema));
            PolicySet cedarPolicies = PolicySet.parsePolicies(test.policy);

            int [] requestCounter = new int [2];
            // Consistency (for the moment at least if based on fully defined request entities)
            List<Request> universe = rsvpAuth.getActionableRequests().stream()
                    .filter(Request::isDefined)
                    .toList();
            for (Request rsvpRequest : universe) {
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
                } else if (cedarDecision == AuthorizationSuccessResponse.Decision.Deny &&
                        rsvpDecision == RequestAuth.Result.Deny) {
                    requestCounter[1]++;
                } else {
                    fail(""" 
                            Request mismatch:
                                RSVP: %s,
                                Cedar: %s
                            RSVP request: %s
                            CedarRequest: %s
                            """.formatted(rsvpDecision, cedarDecision, rsvpRequest, cedarRequest));
                }
            }
            logger.info(GREEN, "Validated Requests (allow/deny): %d (%d/%d)\n",
                    universe.size(), requestCounter[0], requestCounter[1]);
        }
    }
}
