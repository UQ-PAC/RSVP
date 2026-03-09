package uq.pac.rsvp.policy.ast;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;

import com.cedarpolicy.model.exception.InternalException;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class PolicySet extends LinkedHashSet<Policy> implements PolicyItem {

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
            return JsonParser.parsePolicySet(json);
        } catch (InternalException | IOException e) {
            throw new RsvpException("Error parsing policy set in " + policyFile.getFileName(), e);
        }
    }

    /**
     * Parse a Cedar policy file and return the corresponding AST.
     * 
     * @param policies a string containing the Cedar policy file text
     * @return a new PolicySet instance corresponding to the parsed Cedar policy
     *         file
     * @throws InternalException If an error occurs while parsing the Cedar policy
     *                           file
     * @throws IOException       If an IO error occurs while reading the policy file
     */
    public static PolicySet parseCedarPolicySet(String policies) throws RsvpException {
        try {

            String json = com.cedarpolicy.model.policy.PolicySet.parseStringToJsonAst(policies);
            return JsonParser.parsePolicySet(json);
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
}
