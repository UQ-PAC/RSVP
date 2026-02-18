package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.EntityLiteral;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class EntityExpression extends Expression {

    private final List<String> path;
    private final String eid;

    public EntityExpression(String eid, List<String> path, SourceLoc source) {
        super(EntityLiteral, source);
        this.path = path;
        this.eid = eid;
    }

    public EntityExpression(String eid, List<String> path) {
        this(eid, path, SourceLoc.MISSING);
    }

    public String getEid() {
        return eid;
    }

    public List<String> getQualifiedEid() {
        List<String> result = new ArrayList<>(path);
        result.add(eid);
        return result;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitEntityExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitEntityExpr(this);
    }

    @Override
    public String toString() {
        return String.join("::", path) + "::\"" + eid + "\"";
    }

    public static class EntityExpressionDeserialiser implements JsonDeserializer<EntityExpression> {

        @Override
        public EntityExpression deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

            JsonElement src = json.getAsJsonObject().get("source");

            String[] euid = json.getAsJsonObject().get("value").getAsString().split("::");
            int parts = Array.getLength(euid);

            return new EntityExpression(euid[parts - 1], Arrays.asList(euid).subList(0, parts - 1),
                    context.deserialize(src, SourceLoc.class));
        }

    }

}
