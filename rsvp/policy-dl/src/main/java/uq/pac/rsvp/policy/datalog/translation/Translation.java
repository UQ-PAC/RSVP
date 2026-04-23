package uq.pac.rsvp.policy.datalog.translation;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;
import static uq.pac.rsvp.Assertion.require;

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
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.policy.PolicySet;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.invariant.Program;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.DLAtom;
import uq.pac.rsvp.policy.datalog.ast.DLDeclTerm;
import uq.pac.rsvp.policy.datalog.ast.DLFact;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRule;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.ast.DLTerm;
import uq.pac.rsvp.policy.datalog.entity.EntityValidator;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.datalog.invariant.InvariantResult;
import uq.pac.rsvp.policy.datalog.invariant.InvariantValidator;

/**
 * Putting translation of the cedar schema, entities, policies and context together
 */
public class Translation {

    private final Schema schema;
    private final Collection<Policy> policies;
    private final EntitySet entities;
    private final Collection<Invariant> invariants;
    private final Path datalogDir;
    private final DLProgram program;

    private final BiMap<Policy, DLRuleDecl> policyDeclarations;
    private final BiMap<Invariant, DLRuleDecl> invariantDeclarations;

    public Translation(Path schemaFile, Path policiesFile, Path entitiesFile, Path invariantFile, Path datalogDir) {
        this.datalogDir = datalogDir;
        try {
            InputSet validated = validate(schemaFile, policiesFile, entitiesFile, invariantFile);
            this.schema = validated.schema;
            this.entities = validated.entities;
            this.policies = validated.policies;
            this.invariants = validated.invariants;

            this.policyDeclarations = HashBiMap.create();
            int index = 1;
            for (Policy p : policies) {
                policyDeclarations.put(p, TranslationConstants.makePolicyRuleDecl(index++));
            }

            this.invariantDeclarations = HashBiMap.create();
            index = 1;
            for (Invariant i : invariants) {
                invariantDeclarations.put(i, TranslationConstants.makeInvariantRuleDecl(i, index++));
            }

            this.program = translate(schema, policies, entities, invariants);
            program.execute(datalogDir);

        } catch (RsvpException | AuthException | IOException | RuntimeException | InterruptedException | IllegalAccessException e) {
            throw new TranslationError(e);
        }
    }

    record InputSet(Schema schema, Collection<Policy> policies, EntitySet entities, Collection<Invariant> invariants) {}

    static InputSet validate(Path schemaFile, Path policyFile, Path entityFile, Path invariantsFile) throws IOException, AuthException, RsvpException, IllegalAccessException {
        EntitySet entities = EntitySet.parse(entityFile);
        com.cedarpolicy.model.schema.Schema cedarSchema =
                new com.cedarpolicy.model.schema.Schema(Files.readString(schemaFile));
        PolicySet cedarPolicies = PolicySet.parsePolicies(policyFile);

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

        EntityValidationRequest eReq =
                new EntityValidationRequest(cedarSchema,
                        Entities.parse(entityFile).getEntities().stream().toList());
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

        // FIXME: This needs to be moved to entity validation
        // For the moment we also do not support entity names that have the same
        // delimiter that is used for Datalog output (\t)
        List<String> entityNames = entities.getEntities().stream()
                .map(e -> e.getEuid().getId())
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

        // Make sure that the name of the internal record type used for processing
        // records is not used in the schema
        rsvpSchema.entityTypes().forEach(et -> {
            if (et.getName().equals(TmpRecordType)) {
                throw new TranslationError("Internal record type: " + et.getName() + "in schema");
            }
        });

        entities = EntityValidator.validate(rsvpSchema, entities);

        // FIXME: For the moment (while RSVP-native policy validation is not yet ready)
        //        we expect inputs and policies in separate files. This is so we can validate
        //        policies against schema via Cedar utilities. Eventually, however, invariants
        //        and policies files should be merged into one, i.e., a file contains a policy set
        //        that should uphold the invariants from the same file
        Collection<Invariant> invariants = List.of();
        if (invariantsFile != null) {
            Program invariantProgram = Program.parse(invariantsFile);
            if (!invariantProgram.getPolicies().isEmpty()) {
                throw new TranslationError("Invariants found in the policy source: " + policyFile);
            }
            invariants = invariantProgram.getInvariants();
        }

        Program policyProgram = Program.parse(policyFile);
        Collection<Policy> policies = policyProgram.getPolicies();
        if (!policyProgram.getInvariants().isEmpty()) {
            throw new TranslationError("Policies found in the invariant source: " + invariantsFile);
        }

        InvariantValidator invariantValidator = new InvariantValidator(rsvpSchema, entities);
        invariants.forEach(invariantValidator::validate);

        return new InputSet(rsvpSchema, policies, entities, invariants);
    }

    private DLProgram translate(Schema schema, Collection<Policy> policies, EntitySet entities, Collection<Invariant> invariants) {
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
        for (Invariant invariant : invariants) {
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
        invariants.forEach(invariant -> {
            result.put(invariant, new InvariantResult(invariant, loadRelation(invariant)));
        });
        return result;
    }

    public Schema getSchema() {
        return schema;
    }

    public Collection<Policy> getPolicies() {
        return policies;
    }

    public EntitySet getEntities() {
        return entities;
    }

    public Collection<Invariant> getInvariants() {
        return invariants;
    }

    public DLProgram getDatalogProgram() {
        return program;
    }

    public Path getDatalogDir() {
        return datalogDir;
    }
}
