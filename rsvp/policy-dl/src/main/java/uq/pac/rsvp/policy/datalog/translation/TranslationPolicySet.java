package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;

import java.util.*;

/**
 * Translation of a policy set as a collection of datalog statements by each policy
 */
public class TranslationPolicySet {
    private final List<TranslationPolicy> permit;
    private final List<TranslationPolicy> forbid;

    public TranslationPolicySet(PolicySet policies, TranslationSchema schema) {
        List<TranslationPolicy> permitPolicies = new ArrayList<>();
        List<TranslationPolicy> forbidPolicies = new ArrayList<>();

        for (Policy policy: policies) {
            TranslationPolicy translation = new TranslationPolicy(policy, schema);
            (policy.isPermit() ? permitPolicies : forbidPolicies).add(translation);
        }

        this.permit = Collections.unmodifiableList(permitPolicies);
        this.forbid = Collections.unmodifiableList(forbidPolicies);
    }

    public List<TranslationPolicy> getPermitTranslation() {
        return permit;
    }

    public List<TranslationPolicy> getForbidTranslation() {
        return forbid;
    }
}
