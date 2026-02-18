package uq.pac.rsvp.policy.ast.expr;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.policy.ast.PolicyFileEntry;
import uq.pac.rsvp.policy.ast.SourceLoc;

public abstract class Expression extends PolicyFileEntry {

    protected static final Pattern NICE_PROP_NAME = Pattern.compile("[a-zA-Z0-9_]+");

    public static enum ExprType {
        Binary,
        Unary,
        Conditional,
        Call,
        Slot,
        Variable,
        PropertyAccess,
        BooleanLiteral,
        EntityLiteral,
        LongLiteral,
        StringLiteral,
        Record,
        Set
    }

    private final ExprType type;

    protected Expression(ExprType type, SourceLoc source) {
        super(source);
        this.type = type;
    }

    public final boolean isLiteral() {
        return type == ExprType.BooleanLiteral || type == ExprType.EntityLiteral || type == ExprType.LongLiteral
                || type == ExprType.StringLiteral;
    }

    public final boolean isCollection() {
        return type == ExprType.Set || type == ExprType.Record;
    }

    public static class ExpressionDeserialiser implements JsonDeserializer<Expression> {

        @Override
        public Expression deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            String type = json.getAsJsonObject().get("type").getAsString();
            Type expressionType = switch (type) {
                case "binary" -> BinaryExpression.class;
                case "unary" -> UnaryExpression.class;
                case "cond" -> ConditionalExpression.class;
                case "call" -> CallExpression.class;
                case "slot" -> SlotExpression.class;
                case "var" -> VariableExpression.class;
                case "prop" -> PropertyAccessExpression.class;
                case "bool" -> BooleanExpression.class;
                case "euid" -> EntityExpression.class;
                case "long" -> LongExpression.class;
                case "str" -> StringExpression.class;
                case "record" -> RecordExpression.class;
                case "set" -> SetExpression.class;
                default -> throw new JsonParseException("Unknown expression type: " + type);
            };

            return context.deserialize(json, expressionType);
        }
    }
}
