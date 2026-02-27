package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.InternalException;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Putting translation of the cedar schema, entries, context and policies together
 */
public class TranslationDriver {

    public static DLProgram translate(Schema schema, PolicySet policies, Entities entities) {
        TranslationSchema translationSchema = new TranslationSchema(schema);
        TranslationEntitySet translationEntities = new TranslationEntitySet(entities, translationSchema);
        TranslationPolicySet translationPolicies = new TranslationPolicySet(policies, translationSchema);
        DLProgram.Builder builder = new DLProgram.Builder();

        Multimap<String, DLFact> facts = translationEntities.getFacts();

        for (TranslationEntityType type : translationSchema.getTranslationEntityTypes()) {
            builder.comment("Cedar entity: " + type.getName());
            builder.add(type.getEntityRuleDecl());
            builder.add(facts.get(type.getEntityRuleDecl().getName()));

            for (TranslationAttribute attr : type.getAttributes()) {
                builder.comment("");
                builder.comment("Cedar Attribute: " + type.getName() + "." + attr.getName());
                builder.add(attr.getRuleDecl());
                builder.add(facts.get(attr.getRuleDecl().getName()));
            }
            builder.space();
        }

        TranslationAction actionType = new TranslationAction(translationSchema);
        builder.comment("Actions")
                .add(actionType.getAction().getDecl())
                .add(actionType.getAction().getContents())
                .space()
                .comment("ActionPrincipal")
                .add(actionType.getActionPrincipal().getDecl())
                .add(actionType.getActionPrincipal().getContents())
                .space()
                .comment("ActionResource")
                .add(actionType.getActionResource().getDecl())
                .add(actionType.getActionResource().getContents())
                .space();

        builder.comment("Permit Policy Rules");
        for (TranslationPolicy policy : translationPolicies.getPermitTranslation()) {
            builder.comment("Permit Policy: " + policy.getDeclaration().getName());
            builder.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
            builder.space();
        }

        builder.comment("Forbid Policy Rules");
        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            builder.comment("Forbid Policy: " + policy.getDeclaration().getName());
            builder.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
            builder.space();
        }

        builder.comment("General Permit Rule (requests explicitly allowed by the policy)");
        builder.add(TranslationPolicy.makePolicyRuleDecl("Permit"));
        DLAtom permit = TranslationPolicy.makePolicyAtom("Permit");
        for (TranslationPolicy policy : translationPolicies.getPermitTranslation()) {
            DLAtom policyPermit = TranslationPolicy.makePolicyAtom(policy.getName());
            builder.add(new DLRule(permit, policyPermit));
        }
        builder.space();

        builder.comment("General Forbid Rule (requests explicitly forbidden by the policy)");
        builder.add(TranslationPolicy.makePolicyRuleDecl("Forbid"));
        DLAtom forbid = TranslationPolicy.makePolicyAtom("Forbid");
        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            DLAtom policyForbid = TranslationPolicy.makePolicyAtom(policy.getName());
            builder.add(new DLRule(forbid, policyForbid));
        }
        builder.space();
        return builder.build();
    }

    // FIXME: Testing. Remove
    public static void main(String[] args) throws IOException, InternalException {
        Schema schema = Schema.parseCedarSchema(Path.of("examples/photoapp/schema.cedarschema"));
        Entities entities = Entities.parse(Path.of("examples/photoapp/entities.json"));
        PolicySet policies = PolicySet.parseCedarPolicySet(Path.of("examples/photoapp/policy.cedar"));
        DLProgram program = translate(schema, policies, entities);
        System.out.println(program);
    }
}
