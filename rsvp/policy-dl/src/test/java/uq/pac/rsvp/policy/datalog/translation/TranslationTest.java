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
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.junit.jupiter.api.Assertions.*;

public class TranslationTest {

    private final static Logger logger = new Logger();

    @Test
    void translationTest() throws IOException, AuthException, InterruptedException {
        translationTest("photoapp");
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

                AuthorizationRequest cedarRequest  = rsvpRequest.getCedarRequest(cedarSchema);
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
