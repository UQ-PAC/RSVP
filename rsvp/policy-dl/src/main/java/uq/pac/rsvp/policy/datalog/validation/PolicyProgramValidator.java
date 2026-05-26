package uq.pac.rsvp.policy.datalog.validation;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.DetailedError;
import com.cedarpolicy.model.ValidationRequest;
import com.cedarpolicy.model.ValidationResponse;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.policy.PolicySet;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.support.error.TranslationError;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.cedarpolicy.model.schema.Schema.JsonOrCedar.*;

public class PolicyProgramValidator {

    public static void validate(Schema schema, PolicyProgram program) throws AuthException {
        com.cedarpolicy.model.schema.Schema cedarSchema =
                com.cedarpolicy.model.schema.Schema.parse(Cedar, schema.toString());
        String policies = program.policies()
                .map(Policy::toString)
                .collect(Collectors.joining("\n"));

        System.out.println(policies);

        PolicySet cedarPolicies = PolicySet.parsePolicies(policies);

        ValidationRequest vReq = new ValidationRequest(cedarSchema, cedarPolicies);
        AuthorizationEngine engine = new BasicAuthorizationEngine();
        ValidationResponse vResp = engine.validate(vReq);

        if (!vResp.validationPassed()) {
            List<DetailedError> errors = vResp.errors.isPresent() ?
                    vResp.errors.get() : Collections.emptyList();
            String err = errors.stream()
                    .map(e -> e.message)
                    .collect(Collectors.joining("\n"));
            throw new TranslationError("Schema/Policy validation failed: \n" + err);
        }

        InvariantValidator invariantValidator = new InvariantValidator(schema);
        program.invariants().forEach(invariantValidator::validate);
    }
}
