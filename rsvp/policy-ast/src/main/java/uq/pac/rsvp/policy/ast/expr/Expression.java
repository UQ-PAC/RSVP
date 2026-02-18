package uq.pac.rsvp.policy.ast.expr;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

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

    public abstract <T> T compute(PolicyComputationVisitor<T> visitor);

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
