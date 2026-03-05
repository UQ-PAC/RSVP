package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.DetailedError;
import com.cedarpolicy.model.EntityValidationRequest;
import com.cedarpolicy.model.ValidationRequest;
import com.cedarpolicy.model.ValidationResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;

/**
 * Putting translation of the cedar schema, entries, context and policies together
 */
public class Translation {

    private Translation() {}

    private static void validate(Path schemaFile, Path policyFile, Path entityFile) throws IOException, AuthException {
        Entities entities = Entities.parse(entityFile);
        com.cedarpolicy.model.schema.Schema schema =
                new com.cedarpolicy.model.schema.Schema(Files.readString(schemaFile));
        com.cedarpolicy.model.policy.PolicySet policies =
                com.cedarpolicy.model.policy.PolicySet.parsePolicies(policyFile);

        ValidationRequest vReq = new ValidationRequest(schema, policies);
        AuthorizationEngine engine = new BasicAuthorizationEngine();
        ValidationResponse vResp = engine.validate(vReq);

        if (!vResp.validationPassed()) {
            List<DetailedError> errors = vResp.errors.isPresent() ?
                    vResp.errors.get() : Collections.emptyList();
            for (DetailedError error : errors) {
                System.err.println(error);
            }
            throw new TranslationError("Schema/Policy validation failed");
        }

        EntityValidationRequest eReq =
                new EntityValidationRequest(schema, entities.getEntities().stream().toList());
        engine.validateEntities(eReq);
    }

    public static DLProgram translate(Path schemaFile, Path policiesFile, Path entitiesFile) throws IOException, AuthException {
        validate(schemaFile, policiesFile, entitiesFile);

        Schema schema = Schema.parseCedarSchema(schemaFile);
        Entities entities = Entities.parse(entitiesFile);
        PolicySet policies = PolicySet.parseCedarPolicySet(policiesFile);

        return translate(schema, policies, entities);
    }

    private static DLProgram translate(Schema schema, PolicySet policies, Entities entities) {
        TranslationSchema translationSchema = new TranslationSchema(schema);
        TranslationEntitySet translationEntities = new TranslationEntitySet(entities, translationSchema);
        TranslationPolicySet translationPolicies = new TranslationPolicySet(policies, translationSchema);
        DLProgram.Builder builder = new DLProgram.Builder("auth.dl");
        List<DLRuleDecl> output = new ArrayList<>();

        Multimap<String, DLFact> facts = translationEntities.getFacts();

        for (TranslationEntityDefinition type : translationSchema.getTranslationEntityTypes()) {
            builder.comment("Cedar entity: " + type.getName());
            builder.add(type.getEntityRuleDecl());
            builder.add(facts.get(type.getEntityRuleDecl().getName()));

            for (TranslationAttribute attr : type.getAttributes()) {
                builder.nlComment("Cedar Attribute: " + type.getName() + "." + attr.getName());
                builder.add(attr.getRuleDecl());
                builder.add(facts.get(attr.getRuleDecl().getName()));
            }
            builder.space();
        }

        TranslationAction actionType = new TranslationAction(translationSchema);
        builder.comment("Actions")
                .add(actionType.getAction().getStatements())
                .nlComment("Program principals (as specified by the schema)")
                .add(actionType.getActionPrincipal().getStatements())
                .nlComment("Program resources (as specified by the schema)")
                .add(actionType.getActionResource().getStatements())
                .nlComment("Actionable requests")
                .add(actionType.getActionableRequests().getStatements())
                .nlComment("All (potential) principals")
                .add(makePrincipalTypes(translationSchema).getStatements())
                .nlComment("All (potential) resources")
                .add(makeResourceTypes().getStatements())
                .nlComment("All (potential) requests")
                .add(makeAllRequestsRule().getStatements());

        builder.nlComment("Permit Policy Rules");
        for (TranslationPolicy policy : translationPolicies.getPermitTranslation()) {
            builder.comment("Permit Policy: " + policy.getDeclaration().getName());
            builder.add(policy.getDeclaration());
            output.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
            builder.space();
        }

        builder.comment("Forbid Policy Rules");
        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            builder.comment("Forbid Policy: " + policy.getDeclaration().getName());
            builder.add(policy.getDeclaration());
            output.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
            builder.space();
        }

        builder.comment("General Permit Rule (requests explicitly allowed by the policy)");
        builder.add(PermitRuleDecl);
        DLAtom permit = makeStandardAtom(PermitRuleDecl);
        for (TranslationPolicy policy : translationPolicies.getPermitTranslation()) {
            DLAtom policyPermit = makeStandardAtom(policy.getName());
            builder.add(new DLRule(permit, policyPermit));
        }

        builder.nlComment("General Forbid Rule (requests explicitly forbidden by the policy)");
        builder.add(ForbidRuleDecl);
        DLAtom forbid = makeStandardAtom(ForbidRuleDecl);
        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            DLAtom policyForbid = makeStandardAtom(policy.getName());
            builder.add(new DLRule(forbid, policyForbid));
        }

        builder.nlComment("All permitted requests")
            .add(makePermittedRequestsRule().getStatements())
            .nlComment("All forbidden requests")
            .add(makeForbiddenRequestsRule().getStatements())
            .nlComment("I/O")
            .add(makeIODirectives(output));

        return builder.build();
    }

    public static TranslationRule makePrincipalTypes(TranslationSchema schema) {
        List<DLRule> facts = schema.getTranslationEntityTypes().stream()
                .map(e -> {
                    return new DLRule(new DLAtom(PrincipalRuleDecl, PrincipalVar),
                           new DLAtom(e.getEntityRuleDecl(), PrincipalVar));
                })
                .toList();
        return new TranslationRule(PrincipalRuleDecl, facts);
    }

    public static TranslationRule makeResourceTypes() {
        DLRule rule = new DLRule(
                new DLAtom(ResourceRuleDecl, ResourceVar),
                new DLAtom(PrincipalRuleDecl, ResourceVar));
        return new TranslationRule(ResourceRuleDecl, rule);
    }
}
