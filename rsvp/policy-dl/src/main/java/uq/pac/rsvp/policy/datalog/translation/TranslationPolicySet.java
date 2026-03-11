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
    private static final String FORMAT = "Policy";
    private static final String NAME_ANNOTATION = "name";

    public TranslationPolicySet(PolicySet policies, TranslationSchema schema) {
        List<TranslationPolicy> permitPolicies = new ArrayList<>();
        List<TranslationPolicy> forbidPolicies = new ArrayList<>();

        int [] counter = new int [2];
        for (Policy policy: policies) {
            boolean effect = policy.isPermit();
            int index = effect ? 0 : 1;
            String prefix = effect ? "Permit" : "Forbid";
            String annotationName = policy.getAnnotations().get(NAME_ANNOTATION);
            String name = annotationName == null || annotationName.isEmpty() ?
                    FORMAT + ++counter[index] : annotationName;
            name = prefix + name;
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
