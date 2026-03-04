package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.exception.InternalException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uq.pac.rsvp.policy.datalog.DLTest.pathOf;

public class TranslationTest {

    private static final Path ENTITIES = pathOf("photoapp/entities.json");
    private static final Path POLICIES = pathOf("photoapp/policy.cedar");
    private static final Path SCHEMA = pathOf("photoapp/schema.cedarschema");

    Map<String, Policy> getPolicies() throws IOException, InternalException {
        Map<String, Policy> policies = new HashMap<>();
        PolicySet policySet = PolicySet.parseCedarPolicySet(POLICIES);
        for (Policy p : policySet) {
            Map<String, String> annotations = p.getAnnotations();
            // All annotations should be named
            assert (annotations.containsKey("name"));
            String name = annotations.get("name");
            // Names should be unique
            assert(!name.isEmpty() && !policies.containsKey(name));
            if (annotations.containsKey("skip")) {
                System.out.println("Skipping  policy: " + name);
            } else {
                policies.put(name, p);
            }
        }
        return policies;
    }

    @Test
    void TranslationDriverTest() throws IOException, AuthException {
        DLProgram program = Translation.translate(SCHEMA, POLICIES, ENTITIES);
    }

}
