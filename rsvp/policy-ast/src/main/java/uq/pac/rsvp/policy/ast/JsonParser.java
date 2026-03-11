package uq.pac.rsvp.policy.ast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.expr.ActionExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression.EntityExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.ActionExpression.ActionExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.Schema.SchemaDeserialiser;

public class JsonParser {

    private static Gson singleton;

    private static Gson getGson() {

        if (singleton == null) {
            singleton = new GsonBuilder()
                    .registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                    .registerTypeAdapter(ActionExpression.class, new ActionExpressionDeserialiser())
                    .registerTypeAdapter(EntityExpression.class, new EntityExpressionDeserialiser())
                    .registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                    .registerTypeAdapter(Schema.class, new SchemaDeserialiser())
                    .disableJdkUnsafe()
                    .create();
        }

        return singleton;
    }

    public static Schema parseSchema(String json) {
        return getGson().fromJson(json, Schema.class);
    }

    public static PolicySet parsePolicySet(String json) {
        return getGson().fromJson(json, PolicySet.class);
    }
}
