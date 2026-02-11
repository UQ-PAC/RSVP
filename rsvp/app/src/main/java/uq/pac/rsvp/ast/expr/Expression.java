package uq.pac.rsvp.ast.expr;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.ast.SourceLoc;
import uq.pac.rsvp.ast.visitor.PolicyVisitor;

public abstract class Expression {

    protected static final SourceLoc MISSING_SRC = new SourceLoc("unknown", -1, 0);
    protected static final Pattern NICE_PROP_NAME = Pattern.compile("[a-z0-9_]+");

    public static enum ExprType {
        @SerializedName("binary")
        Binary,

        @SerializedName("unary")
        Unary,

        @SerializedName("cond")
        Conditional,

        @SerializedName("call")
        Call,

        @SerializedName("slot")
        Slot,

        @SerializedName("var")
        Variable,

        @SerializedName("prop")
        PropertyAccess,

        @SerializedName("bool")
        BooleanLiteral,

        @SerializedName("euid")
        EntityLiteral,

        @SerializedName("long")
        LongLiteral,

        @SerializedName("str")
        StringLiteral,

        @SerializedName("record")
        Record,

        @SerializedName("set")
        Set
    }

    private final ExprType type;
    private SourceLoc source;

    protected Expression(ExprType type, SourceLoc source) {
        this.type = type;
        this.source = source;
    }

    public abstract void accept(PolicyVisitor visitor);

    public final SourceLoc getSourceLoc() {
        return source != null ? source : MISSING_SRC;
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
            switch (type) {
                case "binary":
                    return context.deserialize(json, BinaryExpression.class);
                case "unary":
                    return context.deserialize(json, UnaryExpression.class);
                case "cond":
                    return context.deserialize(json, ConditionalExpression.class);
                case "call":
                    return context.deserialize(json, CallExpression.class);
                case "slot":
                    return context.deserialize(json, SlotExpression.class);
                case "var":
                    return context.deserialize(json, VariableExpression.class);
                case "prop":
                    return context.deserialize(json, PropertyAccessExpression.class);
                case "bool":
                    return context.deserialize(json, BooleanExpression.class);
                case "euid":
                    return deserialiseEntity(json, context);
                case "long":
                    return context.deserialize(json, LongExpression.class);
                case "str":
                    return context.deserialize(json, StringExpression.class);
                case "record":
                    return context.deserialize(json, RecordExpression.class);
                case "set":
                    return context.deserialize(json, SetExpression.class);
                default:
                    throw new JsonParseException("Unknown expression type: " + type);
            }
        }

        private EntityExpression deserialiseEntity(JsonElement json, JsonDeserializationContext context) {
            JsonElement src = json.getAsJsonObject().get("source");

            String[] euid = json.getAsJsonObject().get("value").getAsString().split("::");
            int parts = Array.getLength(euid);

            return new EntityExpression(euid[parts - 1], Arrays.asList(euid).subList(0, parts - 1),
                    context.deserialize(src, SourceLoc.class));
        }

    }
}
