package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.ActionLiteral;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class ActionExpression extends Expression {
        private final List<String> path;
    private final String eid;

    public ActionExpression(String eid, List<String> path, SourceLoc source) {
        super(ActionLiteral, source);
        this.path = path;
        this.eid = eid;
    }

    public ActionExpression(String eid, List<String> path) {
        this(eid, path, SourceLoc.MISSING);
    }

    // Used by Gson
    @SuppressWarnings("unused")
    private ActionExpression() {
        this(null, null, SourceLoc.MISSING);
    }

    public String getId() {
        return eid;
    }

    public String getQualifiedId() {
        return getPath() + eid;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitActionExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitActionExpr(this);
    }

    @Override
    public String toString() {
        return getPath() + "\"" + eid + "\"";
    }

    private String getPath() {
        if (path.isEmpty()) {
            return "Action::";
        } else {
            return String.join("::", path) + "::Action::";
        }
    }

    public static class ActionExpressionDeserialiser implements JsonDeserializer<ActionExpression> {

        @Override
        public ActionExpression deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

            JsonElement src = json.getAsJsonObject().get("source");

            String[] euid = json.getAsJsonObject().get("value").getAsString().split("::");
            int parts = Array.getLength(euid);

            String quotedName = euid[parts - 1];

            assert parts > 1 && euid[parts - 2].equals("Action");

            return new ActionExpression(
                    quotedName.startsWith("\"") ? quotedName.substring(1, quotedName.length() - 1) : quotedName,
                    Arrays.asList(euid).subList(0, parts - 2),
                    context.deserialize(src, SourceLoc.class));
        }

    }
}
