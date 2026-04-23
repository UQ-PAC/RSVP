package uq.pac.rsvp.policy.ast.deserialisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.deserilisation.SourceLocDeserializer;
import uq.pac.rsvp.policy.ast.expr.ActionExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

public class PolicyJsonParser {

    private static Gson getPolicyGson(String filename, String content) {
        FileSource fs = content == null ? null : new FileSource(filename, content);
        return new GsonBuilder()
                .registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .registerTypeAdapter(ActionExpression.class, new EuidExpressionDeserialiser())
                .registerTypeAdapter(EntityExpression.class, new EuidExpressionDeserialiser())
                .registerTypeAdapter(PolicySet.class, new PolicySetDeserialiser())
                .registerTypeAdapter(SourceLoc.class, new SourceLocDeserializer(fs))
                .disableJdkUnsafe()
                .create();
    }

    public static PolicySet parsePolicySet(String filename, String json, String cedar) {
        return getPolicyGson(filename, cedar).fromJson(json, PolicySet.class);
    }

    public static PolicySet parsePolicySet(String filename, String json) {
        return parsePolicySet(filename, json, null);
    }
}
