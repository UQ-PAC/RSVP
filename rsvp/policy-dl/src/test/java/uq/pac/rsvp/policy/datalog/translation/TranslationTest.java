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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.entity.EntityValidator;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.datalog.invariant.InvariantResult;
import uq.pac.rsvp.StdLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Souffle datalog translation testing
 * <p>
 * Translation tests are directory-based. Each test directory contains a
 * single schema file, a single entities file, a single invariants file and multiple
 * policy files. For each combination of thereof, a single translation test
 * ({@code TestInput}) is run:
 * - Translate to datalog
 * - Generate {@code RequestAuth} object
 * - Cross-check request acceptance/rejection with Cedar
 * - Expect invariants to hold
 */
public class TranslationTest {

    private final static StdLogger logger = new StdLogger();

    private final static Path TESTDIR = Path.of(TestUtil.RESOURCEDIR.toString(), "translation");

    @BeforeAll
    static void configure() {
        logger.setLevel(StdLogger.Level.Info);
    }

    private static class TestInput {
        public final Path policy;
        public final Path schema;
        public final Path entities;
        public final Path invariants;
        public final Path datalogDir;
        public final Path testDir;
        public final String testName;

        private TestInput(Path testDir, Path policy, Path schema, Path entities, Path invariants) {
            this.policy = policy;
            this.schema = schema;
            this.entities = entities;
            this.invariants = invariants;
            this.testDir = testDir;
            this.datalogDir = TestUtil.getDatalogDir(testDir.getFileName().toString(), getPolicyName());
            this.testName = testDir.getFileName().toString();
        }

        public static List<TestInput> load(String dir) {
            Path testDir = Path.of(TESTDIR.toString(), dir);
            Path schema = TestUtil.findFile(testDir, ".cedarschema");
            Path entities = TestUtil.findFile(testDir, ".json");
            Path invariants = TestUtil.findFile(testDir, ".invariant");

            assertTrue(Files.exists(testDir) && Files.isDirectory(testDir));
            assertTrue(Files.exists(schema) && Files.isRegularFile(schema));
            assertTrue(Files.exists(entities) && Files.isRegularFile(entities));
            assertTrue(Files.exists(invariants) && Files.isRegularFile(entities));

            return TestUtil.findFiles(testDir, ".cedar")
                    .stream()
                    .map(policy -> new TestInput(testDir, policy, schema, entities, invariants))
                    .toList();
        }

        public String getName() {
            return testName + "-" + getPolicyName();
        }

        public String getPolicyName() {
            return policy.getFileName().toString().replaceAll("\\.cedar$", "");
        }
    }

    private final static String ONE_OFF = "arith";

    // Running tests for one directory separately for no particular
    // reason apart from being able to launch it separately
    @TestFactory
    Collection<DynamicTest> oneOff() {
        return dynamicTests(ONE_OFF);
    }

    @TestFactory
    Collection<DynamicTest> test() throws IOException {
        List<DynamicTest> tests = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(TESTDIR)) {
            for (Path p : dirs.filter(Files::isDirectory)
                    .filter(d -> !d.getFileName().toString().equals(ONE_OFF))
                    .toList()) {
                tests.addAll(dynamicTests(p.getFileName().toString()));
            }
        }
        return tests;
    }

    // Get a collection of differential dynamic tests from a directory
    Collection<DynamicTest> dynamicTests(String name) {
        return TestInput.load(name).stream()
                .map(t -> DynamicTest.dynamicTest(t.getName(), () -> differentialTest(t)))
                .toList();
    }

    /**
     * Differential test for Cedar and RSVP.
     * The test runs both, RSVP and Cedar authorisation engines and compares the results that should agree
     */
    void differentialTest(TestInput test) throws IOException, AuthException, RsvpException, IllegalAccessException {
        logger.info(YELLOW, "Policy: " + test.policy)
                .info(MAGENTA, "Datalog specification: " + test.datalogDir + "/" + TranslationConstants.ProgramName)
                .fine(CYAN, Files.readString(test.policy));

        long lines = Files.readAllLines(test.policy).stream()
                .map(String::trim)
                .filter(l -> !l.startsWith("//"))
                .filter(l -> l.startsWith("permit") || l.startsWith("forbid"))
                .count();

        if (lines == 0) {
            logger.warning("Empty policy: " + test.policy);
        }

        // Validate Entities
        // FIXME: Add overall validation
        EntitySet rsvpEntities = EntitySet.parse(test.entities);
        uq.pac.rsvp.policy.ast.schema.Schema rsvpSchema =
                uq.pac.rsvp.policy.ast.schema.Schema.parseCedarSchema(test.schema);
        EntityValidator.validate(rsvpSchema, rsvpEntities);

        Translation translation = new Translation(test.schema, test.policy,
                test.entities, test.invariants, test.datalogDir);

        RequestAuth rsvpAuth = new RequestAuth(translation);
        assertTrue(Collections.disjoint(rsvpAuth.getForbiddenRequests().getRequests(),
                rsvpAuth.getPermittedRequests().getRequests()));

        // Check if invariants hold
        Map<Invariant, InvariantResult> invariantResults = translation.getInvariantResult();
        invariantResults.forEach((invariant, result) -> {
            logger.info(YELLOW, invariant.toString());
            logger.bright().bold().info(CYAN, "Holds: " + result.holds());
            logger.bright().bold().info(CYAN, "Assignments: " + result.getAssignments());
            assertTrue(result.holds());
        });

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
                logger.fine(GREEN, rsvpRequest + ":  " + rsvpDecision);
            } else if (cedarDecision == AuthorizationSuccessResponse.Decision.Deny &&
                    rsvpDecision == RequestAuth.Decision.Deny) {
                logger.fine(BLUE, rsvpRequest + ":  " + rsvpDecision);
            } else {
                logger.error(rsvpRequest + ":  RSVP: " + rsvpDecision + " | Cedar:" + cedarDecision);
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
