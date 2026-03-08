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
        public final String dir;
        public final String policyName;
        public final Path policy;
        public final Path schema;
        public final Path entities;

        private TestInput(String dir, Path policy, Path schema, Path entities) {
            this.policy = policy;
            this.schema = schema;
            this.entities = entities;
            this.dir = dir;
            this.policyName = policy.getFileName().toString()
                    .replaceAll(".cedar$", "");
        }

        public static List<TestInput> load(String dir) {
            Path testDir = Path.of(TranslationTest.class.getClassLoader().getResource(dir).getFile());
            Path schema = Path.of(testDir.toString(), "schema.cedarschema");
            Path entities = Path.of(testDir.toString(), "entities.json");

            List<TestInput> inputs = new ArrayList<>();
            try (Stream<Path> p = Files.list(testDir)) {
                p.filter(s -> s.toString().endsWith(".cedar")).forEach(policy -> {
                    inputs.add(new TestInput(dir, policy, schema, entities));
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
        "ancestors"
    })
    void translationOracles(String name) throws AuthException, IOException, InterruptedException {
        Path testDir = Path.of(TranslationTest.class.getClassLoader().getResource(name).getFile());
        Path schemaPath = Path.of(testDir.toString(), "schema.cedarschema");
        Path entitiesPath = Path.of(testDir.toString(), "entities.json");

        List<Path> policyFiles = null;
        try (Stream<Path> p = Files.list(testDir)) {
            policyFiles = p.filter(s -> s.toString().endsWith(".cedar")).toList();
        } catch (IOException e) {
            fail(e);
        }

        for (Path policy : policyFiles) {
            String baseName = policy.getFileName().toString().replaceAll(".cedar$", "");
            Path datalogDir = TestUtil.getDatalogDir(name, baseName);
            RequestAuth rsvpAuth = RequestAuth.load(schemaPath, policy, entitiesPath, datalogDir);

            BiConsumer<Supplier<Set<Request>>, String> doTest = (req, type) -> {
                try {
                    // Get requests from auth
                    String str = req.get().stream()
                            .sorted(Comparator.comparing(Request::getId))
                            .map(Request::getId)
                            .collect(Collectors.joining("\n"));
                    String fn = baseName + "." + type;
                    if (TestUtil.GENERATE_ORACLES) {
                        Path path = Path.of(TestUtil.TESTRESOURCEDIR.toString(), name, fn);
                        Files.writeString(path, str);
                    } else {
                        String oracle = Files.readString(Path.of(testDir.toString(), fn));
                        assertEquals(oracle, str);
                    }
                } catch (IOException e) {
                    fail(e);
                }
            };

            doTest.accept(rsvpAuth::getPermittedRequests, "permitted");
            doTest.accept(rsvpAuth::getForbiddenRequests, "forbidden");
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
