package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.exception.InternalException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static uq.pac.rsvp.policy.datalog.TestUtil.pathOf;

public class TranslationTest {

    private static final Path ENTITIES = pathOf("photoapp/entities.json");
    private static final Path POLICIES = pathOf("photoapp/policy.cedar");
    private static final Path SCHEMA = pathOf("photoapp/schema.cedarschema");

    @Test
    void tt() {
        System.out.println(System.getProperty("test.builddir"));
    }

    PolicySet getPolicySet(Path filename) throws IOException, InternalException {
        Map<String, Policy> policies = new HashMap<>();
        PolicySet policySet = PolicySet.parseCedarPolicySet(filename);
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
        PolicySet set = new PolicySet();
        set.addAll(policies.values());
        return set;
    }

    @Test
    void TranslationDriverTest() throws IOException, AuthException, InterruptedException {
        DLProgram program = Translation.translate(SCHEMA, POLICIES, ENTITIES);
    }
}
