package uq.pac.rsvp.policy.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.cedarpolicy.model.exception.InternalException;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.deserialisation.PolicyJsonParser;

public class PolicySet {

    private final List<Policy> policies;

    public PolicySet(List<Policy> policies) {
        this.policies = List.copyOf(policies);
    }

    /**
     * Parse a Cedar policy file and return the corresponding AST.
     *
     * @param policyFile the path to the Cedar policy file
     * @return a new PolicySet instance corresponding to the parsed Cedar policy
     *         file
     * @throws InternalException If an error occurs while parsing the Cedar policy
     *                           file
     * @throws IOException       If an IO error occurs while reading the policy file
     */
    public static PolicySet parseCedarPolicySet(Path policyFile) throws RsvpException {
        try {
            String json = com.cedarpolicy.model.policy.PolicySet.parseToJsonAst(policyFile);
            return PolicyJsonParser.parsePolicySet(policyFile.toString(), json, Files.readString(policyFile));
        } catch (InternalException | IOException e) {
            throw new RsvpException("Error parsing policy set in " + policyFile.getFileName(), e);
        }
    }

    /**
     * Parse a Cedar policy file and return the corresponding AST.
     *
     * @param filename the name of the file for reporting, if {@code null} then the
     *                 source file for all resulting reports will be
     *                 {@code "unknown"}
     * @param policies a string containing the Cedar policy file text
     * @return a new PolicySet instance corresponding to the parsed Cedar policy
     *         file
     * @throws InternalException If an error occurs while parsing the Cedar policy
     *                           file
     * @throws IOException       If an IO error occurs while reading the policy file
     */
    public static PolicySet parseCedarPolicySet(String filename, String policies) throws RsvpException {
        try {
            String json = com.cedarpolicy.model.policy.PolicySet.parseStringToJsonAst(policies);
            return PolicyJsonParser.parsePolicySet(filename, json, policies);
        } catch (InternalException | IOException e) {
            throw new RsvpException("Error parsing policy set", e);
        }
    }

    public void forEach(Consumer<Policy> consumer) {
        policies.forEach(consumer);
    }

    public Collection<Policy> getPolicies() {
        return policies;
    }

    public Policy getFirst() {
        return policies.getFirst();
    }

    public Stream<Policy> stream() {
        return policies.stream();
    }

    @Override
    public String toString() {
        return policies.toString();
    }
}
