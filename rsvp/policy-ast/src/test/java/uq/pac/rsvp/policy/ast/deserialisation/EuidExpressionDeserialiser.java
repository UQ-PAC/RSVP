package uq.pac.rsvp.policy.ast.deserialisation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import uq.pac.rsvp.policy.ast.expr.ActionExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.EuidExpression;
import uq.pac.rsvp.support.SourceLoc;

import java.lang.reflect.Type;

public class EuidExpressionDeserialiser implements JsonDeserializer<EuidExpression> {

    @Override
    public EuidExpression deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        JsonElement src = json.getAsJsonObject().get("source");
        String value = json.getAsJsonObject().get("value").getAsString();

        int startName = value.indexOf('"');
        int endName = value.lastIndexOf('"');

        String name = "";
        String type = "";

        if (startName == -1) {
            startName = value.lastIndexOf("::");
            name = value.substring(startName + 2);
            type = value.substring(0, Math.max(0, startName));
        } else {
            name = value.substring(startName + 1, endName);
            type = value.substring(0, Math.max(0, startName - 2));
        }

        SourceLoc source = context.deserialize(src, SourceLoc.class);

        if (typeOfT == EntityExpression.class) {
            return new EntityExpression(name, type, source);
        } else if (typeOfT == ActionExpression.class) {
            return new ActionExpression(name, type, source);
        }

        throw new IllegalStateException("Unexpected type of EuidExpression: " + typeOfT.getTypeName());

    }

}
