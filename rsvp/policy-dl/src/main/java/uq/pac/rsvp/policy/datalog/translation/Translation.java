package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.DetailedError;
import com.cedarpolicy.model.EntityValidationRequest;
import com.cedarpolicy.model.ValidationRequest;
import com.cedarpolicy.model.ValidationResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.value.EntityUID;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;

/**
 * Putting translation of the cedar schema, entities, policies and context together
 */
public class Translation {

    private Translation() {}

    static void validate(Path schemaFile, Path policyFile, Path entityFile) throws IOException, AuthException, RsvpException {
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

        // For the moment we do not support arbitrary action names as Cedar does,
        // just standard non-empty identifiers
        Schema rsvpSchema = Schema.parseCedarSchema(schemaFile);
        rsvpSchema.actions().forEach(a -> {
            Arrays.stream(a.getName().split("::")).forEach(s -> {
                if (!s.matches("^[A-ZA-z_][A-Za-z_0-9]+$")) {
                    throw new TranslationError("Unsupported action name: " + a.getName());
                }
            });
        });

        // For the moment we also do not support entity names that have the same
        // delimiter that is used for datalog output (\t)
        List<String> entityNames = entities.getEntities().stream()
                .map(e -> e.getEUID().getId().toString())
                .collect(Collectors.toCollection(ArrayList::new));
        rsvpSchema.entityTypes()
                .forEach(et -> entityNames.addAll(et.getEntityNamesEnum()));
        for (String en : entityNames) {
            if (en.indexOf(OUTPUT_DELIMITER) != -1) {
                throw new TranslationError("Unsupported entity name: " + en);
            }
            if (en.equals(UndefinedEntityUIDName)) {
                throw new TranslationError("Internal entity name in schema: " + en);
            }
        }

        // Make sure that the name of the internal record type used for processing records is not used in the schema
        rsvpSchema.entityTypes().forEach(et -> {
            if (et.getName().equals(TmpRecordType.getName())) {
                throw new TranslationError("Internal record type: " + et.getName() + "in schema");
            }
        });
    }

    public static DLProgram translate(Path schemaFile, Path policiesFile, Path entitiesFile) throws IOException, AuthException, RsvpException {
        validate(schemaFile, policiesFile, entitiesFile);
        Schema schema = Schema.parseCedarSchema(schemaFile);
        Entities entities = Entities.parse(entitiesFile);

        // Strictly speaking we expect a complete closed world in that we operate on the set of provided entities.
        // One exception is the enum-style definition of entities. The translation assumes they exist as well,
        // but here we allow them to be generated if they are not provided.
        Set<EntityUID> entitiesEuids = entities.getEntities().stream().map(Entity::getEUID).collect(Collectors.toSet());
        Set<Entity> newEntities = new HashSet<>();
        schema.entityTypeNames().stream().map(schema::getEntityType).forEach(ed -> {
            ed.getEntityNamesEnum().forEach(en -> {
                EntityUID uid = EntityUID.parse("%s::\"%s\"".formatted(ed.getName(), en)).orElseThrow();
                if (!entitiesEuids.contains(uid)) {
                    newEntities.add(new Entity(uid));
                }
            });
        });

        if (!newEntities.isEmpty()) {
            newEntities.addAll(entities.getEntities());
            entities = new Entities(newEntities);
        }

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
        }

        // Attribute relation
        builder.comment("Attribute")
                .add(AttributeRuleDecl)
                .add(facts.get(AttributeRuleDecl.getName()));

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
        builder.comment("ParentOf")
                .add(ParentOfRuleDecl)
                .add(facts.get(ParentOfRuleDecl.getName()))
                .add(actions.getParentOfFacts());

        // Parent of relation should be transitive, make it so
        DLRule rpt = new DLRule(new DLAtom(ParentOfRuleDecl, DLTerm.var("x"), DLTerm.var("z")),
                new DLAtom(ParentOfRuleDecl, DLTerm.var("x"), DLTerm.var("y")),
                new DLAtom(ParentOfRuleDecl, DLTerm.var("y"), DLTerm.var("z")));
        builder.comment("Add transitivity to ParentOf")
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
            DLAtom policyPermit = makeStandardAtom(policy.getDeclaration());
            builder.add(new DLRule(permit, policyPermit));
        }

        builder.comment("General Forbid Rule (requests explicitly forbidden by the policy)")
                .add(ForbidRuleDecl);
        DLAtom forbid = makeStandardAtom(ForbidRuleDecl);
        for (TranslationPolicy policy : translationPolicies.getForbidTranslation()) {
            DLAtom policyForbid = makeStandardAtom(policy.getDeclaration());
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
