package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;

import java.util.*;

public class TranslationPolicySet {
    private final List<TranslationPolicy> permit;
    private final List<TranslationPolicy> forbid;
    private static final String FORMAT = "CedarPolicy";

    public TranslationPolicySet(PolicySet policies, TranslationSchema schema) {
        List<TranslationPolicy> permitPolicies = new ArrayList<>();
        List<TranslationPolicy> forbidPolicies = new ArrayList<>();

        int [] counter = new int [2];
        for (Policy policy: policies) {
            boolean effect = policy.isPermit();
            int index = effect ? 0 : 1;
            String name = (effect ? "Permit" : "Forbid") + FORMAT + ++counter[index];
            TranslationPolicy translation = new TranslationPolicy(name, policy, schema);
            List<TranslationPolicy> target = effect ? permitPolicies : forbidPolicies;
            target.add(translation);
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
