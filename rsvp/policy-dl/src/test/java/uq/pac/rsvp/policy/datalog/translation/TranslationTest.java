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
import org.junit.jupiter.api.Test;
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

    @Test
    void translationTest() throws IOException, AuthException, InterruptedException {
        translationTest("photoapp");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ancestors",
        "photoapp"
    })
    void translationOracles(String name) throws AuthException, IOException, InterruptedException {
        List<TestInput> tests = TestInput.load(name);

        for (TestInput test : tests) {
            RequestAuth rsvpAuth = RequestAuth.load(test.schema, test.policy, test.entities, test.datalogDir);
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
     * This test accepts a resource directory that contains the following files:
     * - 'schema.cedarschema' : cedar schema
     * - 'entities.json' : cedar entities
     * - '*.cedar' cedar policies
     * The test runs both, RSVP and Cedar authorisation engines and compares the results that should agree
     */
    void translationTest(String app) throws IOException, AuthException, InterruptedException {
        String test = TranslationTest.class.getClassLoader().getResource(app).getFile();
        Path testDir = Path.of(test);

        Path schemaPath = Path.of(test, "schema.cedarschema");
        Path entitiesPath = Path.of(test, "entities.json");
        List<Path> policiesPath;
        try (Stream<Path> p = Files.list(testDir)) {
            policiesPath = p.filter(s -> s.toString().endsWith(".cedar")).toList();
        }

        assertFalse(policiesPath.isEmpty());
        assertTrue(Files.exists(schemaPath));
        assertTrue(Files.exists(entitiesPath));

        for (Path policy : policiesPath) {
            String baseName = policy.getFileName().toString().replaceAll(".cedar$", "");
            Path datalogDir = TestUtil.getDatalogDir(app, baseName);
            RequestAuth rsvpAuth = RequestAuth.load(schemaPath, policy, entitiesPath, datalogDir);
            assertTrue(Collections.disjoint(rsvpAuth.getForbiddenRequests(), rsvpAuth.getPermittedRequests()));

            logger.info(YELLOW, "Policy: " + policy)
                .info(CYAN, Files.readString(policy))
                .info(MAGENTA, "Datalog directory: " + datalogDir);

            AuthorizationEngine cedarAuth = new BasicAuthorizationEngine();
            Entities cedarEntities = Entities.parse(entitiesPath);

            Schema cedarSchema = Schema.parse(Schema.JsonOrCedar.Cedar, Files.readString(schemaPath));
            PolicySet cedarPolicies = PolicySet.parsePolicies(policy);

            int [] requestCounter = new int [2];
            Set<Request> universe = rsvpAuth.getActionableRequests();
            for (Request rsvpRequest : universe) {
                RequestAuth.Result rsvpDecision = rsvpAuth.authorize(rsvpRequest);
                assertTrue(rsvpDecision == RequestAuth.Result.DENY ||
                        rsvpDecision == RequestAuth.Result.ALLOW);

                AuthorizationRequest cedarRequest = rsvpRequest.getCedarRequest(cedarSchema);
                AuthorizationResponse cedarResponse =
                        cedarAuth.isAuthorized(cedarRequest, cedarPolicies, cedarEntities);
                assertEquals(AuthorizationResponse.SuccessOrFailure.Success, cedarResponse.type, () -> """
                        %s
                        """.formatted(cedarResponse.errors.orElseThrow(AssertionError::new)));
                AuthorizationSuccessResponse cedarSuccess =
                        cedarResponse.success.orElseThrow(AssertionError::new);
                AuthorizationSuccessResponse.Decision cedarDecision = cedarSuccess.getDecision();

                if (cedarDecision == AuthorizationSuccessResponse.Decision.Allow &&
                        rsvpDecision == RequestAuth.Result.ALLOW) {
                    requestCounter[0]++;
                } else if (cedarDecision == AuthorizationSuccessResponse.Decision.Deny &&
                        rsvpDecision == RequestAuth.Result.DENY) {
                    requestCounter[1]++;
                } else {
                    fail(""" 
                            Request mismatch:
                                RSVP: %s,
                                Cedar: %s
                            """.formatted(rsvpDecision.toString(), cedarDecision.toString()));
                }
            }
            logger.info(GREEN, "Validated Requests (allow/deny): %d (%d/%d)\n",
                    universe.size(), requestCounter[0], requestCounter[1]);
        }
    }
}
