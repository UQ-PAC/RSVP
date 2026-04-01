package uq.pac.rsvp.policy.datalog.translation;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.AttributeRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.ForbidRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.HasAttributeRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.NullifiedRequestsRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.OUTPUT_DELIMITER;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.OUTPUT_EXT;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.ParentOfRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.PermitRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.PermittedRequestsRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.ProgramName;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.TmpRecordType;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.UndefinedEntityUIDName;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.makeAtom;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.makeForbiddenRequestsRule;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.makeIODirectives;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.makePermittedRequestsRule;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.DLAtom;
import uq.pac.rsvp.policy.datalog.ast.DLDeclTerm;
import uq.pac.rsvp.policy.datalog.ast.DLFact;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRule;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.ast.DLTerm;
import uq.pac.rsvp.policy.datalog.invariant.Invariant;
import uq.pac.rsvp.policy.datalog.invariant.InvariantResult;
import uq.pac.rsvp.policy.datalog.invariant.InvariantSet;

/**
 * Putting translation of the cedar schema, entities, policies and context together
 */
public class Translation {

    private final Schema schema;
    private final PolicySet policies;
    private final Entities entities;
    private final InvariantSet invariants;
    private final Path datalogDir;
    private final DLProgram program;

    private final BiMap<Policy, DLRuleDecl> policyDeclarations;
    private final BiMap<Invariant, DLRuleDecl> invariantDeclarations;

    public Translation(Path schemaFile, Path policiesFile, Path entitiesFile, Path invariantFile, Path datalogDir) {
        this.datalogDir = datalogDir;
        try {
            validate(schemaFile, policiesFile, entitiesFile);
            this.schema = Schema.parseCedarSchema(schemaFile);
            this.entities = updateEntities(Entities.parse(entitiesFile), schema);
            this.policies = PolicySet.parseCedarPolicySet(policiesFile);
            this.invariants = invariantFile == null ? InvariantSet.parse("") : InvariantSet.parse(invariantFile);

            this.policyDeclarations = HashBiMap.create();
            int index = 1;
            for (Policy p : policies) {
                policyDeclarations.put(p, TranslationConstants.makePolicyRuleDecl(p, index++));
            }

            this.invariantDeclarations = HashBiMap.create();
            index = 1;
            for (Invariant i : invariants.getInvariants()) {
                invariantDeclarations.put(i, TranslationConstants.makeInvariantRuleDecl(i, index++));
            }

            this.program = translate(schema, policies, entities, invariants);
            program.execute(datalogDir);

        } catch (RsvpException | AuthException | IOException | RuntimeException | InterruptedException e) {
            throw new TranslationError(e);
        }
    }

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
            if (en.contains(OUTPUT_DELIMITER)) {
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

        // FIXME: Invariant validation
    }

    /**
     * Update the initial set of entities to
     * - remove actions (as entities)
     * - add implicit entities from enums
     */
    public static Entities updateEntities(Entities entities, Schema schema) {
        // When we are given an entity set it can also include actions,
        // remove those from the set of entities, as we treat them differently
        // and pull action-related information from the schema
        Set<Entity> entitySet = entities.getEntities().stream()
                .filter(e -> {
                    String type = e.getEUID().getType().toString();
                    return !type.equals("Action") && !type.endsWith("::Action");
                }).collect(Collectors.toCollection(HashSet::new));

        // Strictly speaking we expect a complete closed world in that we operate on the set of provided entities.
        // One exception is the enum-style definition of entities. The translation assumes they exist as well,
        // but here we allow them to be generated if they are not provided.
        Set<EntityUID> entitiesEuids = entitySet.stream().map(Entity::getEUID).collect(Collectors.toSet());
        schema.entityTypeNames().stream().map(schema::getEntityType).forEach(ed -> {
            ed.getEntityNamesEnum().forEach(en -> {
                EntityUID uid = EntityUID.parse("%s::\"%s\"".formatted(ed.getName(), en)).orElseThrow();
                if (!entitiesEuids.contains(uid)) {
                    entitySet.add(new Entity(uid));
                }
            });
        });
        return new Entities(entitySet);
    }

    private DLProgram translate(Schema schema, PolicySet policies, Entities entities, InvariantSet invariants) {
        TranslationSchema translationSchema = new TranslationSchema(schema);
        TranslationEntitySet translationEntities = new TranslationEntitySet(entities, translationSchema);
        Collection<TranslationPolicy> translationPermitPolicies = policies.stream()
                .filter(Policy::isPermit)
                .map(p -> new TranslationPolicy(p, policyDeclarations.get(p), translationSchema))
                .toList();
        Collection<TranslationPolicy> translationForbidPolicies = policies.stream()
                .filter(Policy::isForbid)
                .map(p -> new TranslationPolicy(p, policyDeclarations.get(p), translationSchema))
                .toList();

        DLProgram.Builder builder = new DLProgram.Builder(ProgramName);
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
        builder.comment("Attribute values")
                .add(AttributeRuleDecl)
                .add(facts.get(AttributeRuleDecl.getName()));

        // Attribute relation
        builder.comment("Attribute existence")
                .add(HasAttributeRuleDecl)
                .add(facts.get(HasAttributeRuleDecl.getName()));

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

        for (TranslationPolicy policy : translationPermitPolicies) {
            builder.comment("Permit Policy: " + policy.getDeclaration().getName())
                    .add(policy.getDeclaration());
            output.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
        }

        for (TranslationPolicy policy : translationForbidPolicies) {
            builder.comment("Forbid Policy: " + policy.getDeclaration().getName())
                    .add(policy.getDeclaration());
            output.add(policy.getDeclaration());
            for (DLRule rule : policy.getRules()) {
                builder.add(rule);
            }
        }

        builder.comment("General Permit Rule (requests explicitly allowed by the policy)")
                .add(PermitRuleDecl);
        DLAtom permit = makeAtom(PermitRuleDecl);
        for (TranslationPolicy policy : translationPermitPolicies) {
            DLAtom policyPermit = makeAtom(policy.getDeclaration());
            builder.add(new DLRule(permit, policyPermit));
        }

        builder.comment("General Forbid Rule (requests explicitly forbidden by the policy)")
                .add(ForbidRuleDecl);
        DLAtom forbid = makeAtom(ForbidRuleDecl);
        for (TranslationPolicy policy : translationForbidPolicies) {
            DLAtom policyForbid = makeAtom(policy.getDeclaration());
            builder.add(new DLRule(forbid, policyForbid));
        }

        builder.comment("All permitted requests")
            .add(makePermittedRequestsRule().getStatements())
            .comment("All forbidden requests")
            .add(makeForbiddenRequestsRule().getStatements());

        builder.comment("Invariants");
        for (Invariant invariant : invariants.getInvariants()) {
            TranslationInvariant ti = new TranslationInvariant(invariant,
                    invariantDeclarations.get(invariant), translationSchema);
            builder.add(ti.getDeclaration());
            output.add(ti.getDeclaration());
            ti.getRules().forEach(builder::add);
        }

        builder.comment("I/O")
            .add(makeIODirectives(output));

        return builder.build();
    }

    /**
     * Assuming the datalog output .csv file contains requests of the form {@code principal TAB resource TAB action}
     * load them as a request set
     *
     * @param csv CSV results file within that directory
     * @param name name of the policy that generated that result
     */
    private RequestSet loadRequests(Path csv, String name) {
        Path dlOut = Path.of(datalogDir.toString(), csv.getFileName().toString());

        if (!Files.exists(dlOut) || !Files.isRegularFile(dlOut)) {
            throw new TranslationError("Target CSV file %s does not exist or not a directory".formatted(dlOut));
        }

        BufferedReader reader;
        Set<Request> set = new HashSet<>();
        try {
            reader = new BufferedReader(new FileReader(dlOut.toFile()));
            String line = reader.readLine();
            while (line != null) {
                set.add(new Request(line.trim()));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throw new TranslationError(e.getMessage());
        }
        return new RequestSet(set, name);
    }

    RequestSet loadRequests(Policy policy) {
        return loadRequests(policyDeclarations.get(policy));
    }

    Relation loadRelation(DLRuleDecl decl) {
        List<String> headers = decl.getDeclTerms().stream().map(DLDeclTerm::getName).toList();
        Path csv = Path.of(datalogDir.toString(), decl.getName() + OUTPUT_EXT);
        if (!Files.exists(csv) || !Files.isRegularFile(csv)) {
            throw new TranslationError("Target CSV file %s does not exist or not a directory".formatted(csv));
        }

        BufferedReader reader;
        List<List<String>> rows = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(csv.toFile()));
            String line = reader.readLine();
            while (line != null) {
                List<String> row = Pattern.compile("\\t")
                        .splitAsStream(line)
                        .toList();
                rows.add(row);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throw new TranslationError(e.getMessage());
        }
        return new Relation(headers, rows);
    }

    Relation loadRelation(Invariant invariant) {
        return loadRelation(invariantDeclarations.get(invariant));
    }

    RequestSet loadRequests(DLRuleDecl decl) {
        require(PermittedRequestsRuleDecl.getDeclTerms().equals(decl.getDeclTerms()));
        Path csv = Path.of(decl.getName() + OUTPUT_EXT);
        return loadRequests(csv, decl.getName());
    }

    public Map<Policy, RequestSet> getPolicyResult() {
        Map<Policy, RequestSet> requests = new HashMap<>();
        policies.forEach(policy -> requests.put(policy, loadRequests(policy)));
        return requests;
    }

    public Map<Invariant, InvariantResult> getInvariantResult() {
        Map<Invariant, InvariantResult> result = new HashMap<>();
        invariants.getInvariants().forEach(invariant -> {
            result.put(invariant, new InvariantResult(invariant, loadRelation(invariant)));
        });
        return result;
    }

    public Schema getSchema() {
        return schema;
    }

    public PolicySet getPolicies() {
        return policies;
    }

    public Entities getEntities() {
        return entities;
    }

    public InvariantSet getInvariants() {
        return invariants;
    }

    public DLProgram getProgram() {
        return program;
    }

    public Path getDatalogDir() {
        return datalogDir;
    }
}
