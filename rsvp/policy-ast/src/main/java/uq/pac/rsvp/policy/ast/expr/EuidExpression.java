package uq.pac.rsvp.policy.ast.expr;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import uq.pac.rsvp.support.SourceLoc;

public abstract class EuidExpression extends Expression {
    private final String entityType;
    private final String eid;

    protected EuidExpression(String eid, String entityType, SourceLoc source) {
        super(source);
        this.entityType = entityType;
        this.eid = eid;
    }

    /**
     * Get the type of this entity in the format {@code Namespace::Type}.
     * 
     * @return the qualified type of this entity
     */
    public final String getType() {
        return entityType;
    }

    /**
     * Get the unquoted, unqualified EID of this entity.
     * 
     * @return the EID of this entity
     */
    public final String getName() {
        return eid;
    }

    /**
     * Return the fully qualified name of this entity in the format
     * {@code Namespace::Type::"entityName"}
     * 
     * @return The fully qualified name of this entity
     */
    public final String getQualifiedName() {
        return entityType + "::\"" + eid + "\"";
    }

    @Override
    public final String toString() {
        return getQualifiedName();
    }

    public static class EuidExpressionDeserialiser implements JsonDeserializer<EuidExpression> {

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
}
