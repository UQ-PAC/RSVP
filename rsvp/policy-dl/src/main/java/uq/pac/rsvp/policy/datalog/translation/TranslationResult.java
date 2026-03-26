package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.DLDeclTerm;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.invariant.Invariant;
import uq.pac.rsvp.policy.datalog.invariant.InvariantSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Objects of this class that capture results of cedar-to-datalog translation.
 * Objects of this class should be obtained via calls to
 * {@link Translation#translate(Path, Path, Path, Path, Path)}
 */
public class TranslationResult {

    private final Schema schema;
    private final PolicySet policies;
    private final Entities entities;
    private final InvariantSet invariants;
    private final DLProgram program;
    private final Path datalogDir;

    TranslationResult(Schema schema, PolicySet policies, Entities entities,
                      InvariantSet invariants, DLProgram program, Path datalogDir) {
        this.schema = schema;
        this.policies = policies;
        this.entities = entities;
        this.invariants = invariants;
        this.program = program;
        this.datalogDir = datalogDir;
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
        Path csv = Path.of(makePolicyRuleDecl(policy).getName() + OUTPUT_EXT);
        return loadRequests(csv, policy.getName());
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

    Relation loadRelation(Path dir, Invariant invariant) {
        return loadRelation(makeInvariantRuleDecl(invariant));
    }

    RequestSet loadRequests(DLRuleDecl decl) {
        require(PermittedRequestsRuleDecl.getDeclTerms().equals(decl.getDeclTerms()));
        Path csv = Path.of(decl.getName() + OUTPUT_EXT);
        return loadRequests(csv, decl.getName());
    }

    Map<Policy, RequestSet> getPolicyResult() {
        Map<Policy, RequestSet> requests = new HashMap<>();
        policies.forEach(policy -> {
            requests.put(policy, loadRequests(policy));
        });
        return requests;
    }

    Map<Invariant, Relation> getInvariantRelations() {
        Map<Invariant, Relation> relations = new HashMap<>();
        invariants.getInvariants().forEach(invariant -> {
            relations.put(invariant, loadRelation(datalogDir, invariant));
        });
        return relations;
    }
}
