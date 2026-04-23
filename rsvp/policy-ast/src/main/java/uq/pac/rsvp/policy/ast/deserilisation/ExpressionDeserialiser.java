package uq.pac.rsvp.policy.ast.deserilisation;

import com.google.gson.*;
import uq.pac.rsvp.policy.ast.expr.*;

import java.lang.reflect.Type;

public class ExpressionDeserialiser implements JsonDeserializer<Expression> {

    @Override
    public Expression deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        Type expressionType = switch (type) {
            case "binary" -> BinaryExpression.class;
            case "unary" -> UnaryExpression.class;
            case "cond" -> ConditionalExpression.class;
            case "call" -> CallExpression.class;
            case "slot" -> SlotExpression.class;
            case "var" -> VariableExpression.class;
            case "prop" -> PropertyAccessExpression.class;
            case "action" -> ActionExpression.class;
            case "bool" -> BooleanExpression.class;
            case "euid" -> EntityExpression.class;
            case "long" -> LongExpression.class;
            case "str" -> StringExpression.class;
            case "record" -> RecordExpression.class;
            case "set" -> SetExpression.class;
            case "type" -> TypeExpression.class;
            default -> throw new JsonParseException("Unknown expression type: " + type);
        };

        return context.deserialize(json, expressionType);
    }
}
