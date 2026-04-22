package uq.pac.rsvp.policy.ast;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.cedarpolicy.model.exception.InternalException;

import com.google.gson.*;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

//FIXME: Remove in favour of program
public class PolicySet extends PolicyAstNode {

    private final List<Policy> policies;

    public PolicySet(List<Policy> policies, SourceLoc location) {
        // FIXME: Locations based on policies
        super(location);
        this.policies = List.copyOf(policies);
    }

    public PolicySet(List<Policy> policies) {
        this(policies, SourceLoc.MISSING);
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
            return JsonParser.parsePolicySet(policyFile.toString(), json, Files.readString(policyFile));
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
            return JsonParser.parsePolicySet(filename, json, policies);
        } catch (InternalException | IOException e) {
            throw new RsvpException("Error parsing policy set", e);
        }
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitPolicySet(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitPolicySet(this);
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

    public static class PolicySetDeserialiser implements JsonDeserializer<PolicySet> {
        @Override
        public PolicySet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonArray array = json.getAsJsonArray();
            List<Policy> policies = new ArrayList<>();
            for (JsonObject o : array.asList().stream().map(JsonElement::getAsJsonObject).toList()) {
                policies.add(context.deserialize(o, Policy.class));
            }
            return new PolicySet(policies);
        }
    }

}
