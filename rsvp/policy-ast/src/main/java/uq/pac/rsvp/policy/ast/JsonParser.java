package uq.pac.rsvp.policy.ast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.expr.ActionExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.EuidExpression.EuidExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.Schema.SchemaDeserialiser;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.SourceLoc.SourceLocDeserializer;

public class JsonParser {

    private static Gson getGson(String filename, String content) {

        return new GsonBuilder()
                .registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .registerTypeAdapter(ActionExpression.class, new EuidExpressionDeserialiser())
                .registerTypeAdapter(EntityExpression.class, new EuidExpressionDeserialiser())
                .registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                .registerTypeAdapter(Schema.class, new SchemaDeserialiser())
                .registerTypeAdapter(SourceLoc.class, new SourceLocDeserializer(filename, content))
                .disableJdkUnsafe()
                .create();

    }

    public static Schema parseSchema(String json) {
        return parseSchema(null, json);
    }

    // FIXME: no schema line numbers anyway
    public static Schema parseSchema(String filename, String json) {
        return getGson(filename, json).fromJson(json, Schema.class);
    }

    public static PolicySet parsePolicySet(String filename, String json, String cedar) {
        return getGson(filename, cedar).fromJson(json, PolicySet.class);
    }
}
