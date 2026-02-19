package uq.pac.rsvp.policy.ast;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;

import com.cedarpolicy.model.exception.InternalException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression.EntityExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;

public class PolicySet extends LinkedHashSet<Policy> {

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
    public static PolicySet parseCedarPolicySet(Path policyFile) throws InternalException, IOException {
        String json = com.cedarpolicy.model.policy.PolicySet.parseToJsonAst(policyFile);
        Gson gson = new GsonBuilder().registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .registerTypeAdapter(EntityExpression.class, new EntityExpressionDeserialiser())
                .create();
        return gson.fromJson(json, PolicySet.class);
    }
}
