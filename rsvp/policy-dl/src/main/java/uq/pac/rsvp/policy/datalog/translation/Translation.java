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
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            String err = errors.stream()
                    .map(e -> e.message)
                    .collect(Collectors.joining("\n"));
            throw new TranslationError("Schema/Policy validation failed: \n" + err);
        }

        EntityValidationRequest eReq =
                new EntityValidationRequest(schema, entities.getEntities().stream().toList());
        engine.validateEntities(eReq);
    }

    public static DLProgram translate(Path schemaFile, Path policiesFile, Path entitiesFile) throws IOException, AuthException, RsvpException {
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

        // All facts gathered from entities
        Multimap<String, DLFact> facts = translationEntities.getFacts();

        for (TranslationEntityDefinition type : translationSchema.getDefinitions()) {
            // Entity definition facts
            builder.comment("Entity: " + type.getName())
                .add(type.getEntityRuleDecl())
                .add(facts.get(type.getEntityRuleDecl().getName()));

            // Entity attribute facts
            for (TranslationAttribute attr : type.getAttributes()) {
                builder.comment("Attribute: " + type.getName() + "." + attr.getName())
                    .add(attr.getRuleDecl())
                    .add(facts.get(attr.getRuleDecl().getName()));
            }
        }

        TranslationAction actions = new TranslationAction(translationSchema);
        builder.comment("Actions")
                .add(actions.getAction().getStatements())
                .comment("Program principals")
                .add(actions.getActionPrincipal().getStatements())
                .comment("Program resources")
                .add(actions.getActionResource().getStatements())
                .comment("Actionable requests")
                .add(actions.getActionableRequests().getStatements())
                .comment("Empty relation")
                .add(NullifiedRequestsRuleDecl);

        // ParentOf relation
        builder.comment("Parents")
                .add(ParentOfRuleDecl)
                .add(facts.get(ParentOfRuleDecl.getName()))
                .add(actions.getParentOfFacts());

        // Parent of relation should be transitive, make it so
        DLRule rpt = new DLRule(new DLAtom(ParentOfRuleDecl, DLTerm.var("x"), DLTerm.var("z")),
                new DLAtom(ParentOfRuleDecl, DLTerm.var("x"), DLTerm.var("y")),
                new DLAtom(ParentOfRuleDecl, DLTerm.var("y"), DLTerm.var("z")));
        builder.comment("Transitive Parents")
                .add(rpt);

        for (TranslationPolicy policy : translationPolicies.getPermitTranslation()) {
            builder.comment("Permit Policy: " + policy.getDeclaration().getName())
                    .add(policy.getDeclaration());
            output.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
        }

        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            builder.comment("Forbid Policy: " + policy.getDeclaration().getName())
                    .add(policy.getDeclaration());
            output.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
        }

        builder.comment("General Permit Rule (requests explicitly allowed by the policy)")
                .add(PermitRuleDecl);
        DLAtom permit = makeStandardAtom(PermitRuleDecl);
        for (TranslationPolicy policy : translationPolicies.getPermitTranslation()) {
            DLAtom policyPermit = makeStandardAtom(policy.getName());
            builder.add(new DLRule(permit, policyPermit));
        }

        builder.comment("General Forbid Rule (requests explicitly forbidden by the policy)")
                .add(ForbidRuleDecl);
        DLAtom forbid = makeStandardAtom(ForbidRuleDecl);
        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            DLAtom policyForbid = makeStandardAtom(policy.getName());
            builder.add(new DLRule(forbid, policyForbid));
        }

        builder.comment("All permitted requests")
            .add(makePermittedRequestsRule().getStatements())
            .comment("All forbidden requests")
            .add(makeForbiddenRequestsRule().getStatements());

        builder
            .comment("I/O")
            .add(makeIODirectives(output));

        return builder.build();
    }
}
