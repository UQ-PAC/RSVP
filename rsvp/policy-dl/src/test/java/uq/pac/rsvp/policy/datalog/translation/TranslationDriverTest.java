package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.InternalException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslationDriverTest {

    private static final Path ENTITIES = Path.of("examples/photoapp/entities.json");
    private static final Path POLICIES = Path.of("examples/photoapp/policy.cedar");
    private static final Path SCHEMA = Path.of("examples/photoapp/schema.cedarschema");

    Map<String, Policy> getPolicies() throws IOException, InternalException {
        Map<String, Policy> policies = new HashMap<>();
        PolicySet policySet = PolicySet.parseCedarPolicySet(POLICIES);
        for (Policy p : policySet) {
            Map<String, String> annotations = p.getAnnotations();
            assertEquals(1, annotations.size());
            String annotation = annotations.keySet().stream().findFirst().orElse(null);
            if (!annotations.get(annotation).equalsIgnoreCase("unsupported")) {
                policies.put(annotation, p);
            }
        }
        return policies;
    }

    @Test
    public void test() throws IOException, InternalException {
        Entities entities = Entities.parse(ENTITIES);
        Schema schema = Schema.parseCedarSchema(SCHEMA);
        TranslationSchema translationSchema = new TranslationSchema(schema);

        TranslationTyping types = new TranslationTyping(schema);
        Map<String, Policy> policies = getPolicies();

        policies.forEach((annotation, policy) -> {
            System.out.println(" === " + annotation + " =========== ");
            DLRelationDecl declaration = new DLRelationDecl(annotation,
                    new DLDeclTerm("principal", DLType.SYMBOL),
                    new DLDeclTerm("resource", DLType.SYMBOL),
                    new DLDeclTerm("action", DLType.SYMBOL));
            System.out.println(declaration);

            List<List<Expression>> disjunctions = NFConverter.toDNF(policy.getCondition());
            for (List<Expression> disjunction : disjunctions) {
                DLRule rule = TranslationVisitor.translate(translationSchema, disjunction, declaration);
                System.out.println(rule);
            }
        });
    }
}
