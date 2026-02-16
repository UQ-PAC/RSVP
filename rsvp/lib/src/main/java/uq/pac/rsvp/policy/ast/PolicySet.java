package uq.pac.rsvp.policy.ast;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;

import com.cedarpolicy.model.exception.InternalException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;

public class PolicySet extends LinkedHashSet<Policy> {

    public static PolicySet parseCedarPolicySet(Path policyFile) throws InternalException, IOException {
        String json = com.cedarpolicy.model.policy.PolicySet.parseToJsonAst(policyFile);
        Gson gson = new GsonBuilder().registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .create();
        return gson.fromJson(json, PolicySet.class);
    }
}
