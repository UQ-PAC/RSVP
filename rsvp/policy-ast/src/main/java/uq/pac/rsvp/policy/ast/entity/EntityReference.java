package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.text.StringEscapeUtils;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Objects;

/**
 * A reference to en entity, such as Account::"Alice"
 */
public class EntityReference extends EntityValue {
    private final String type;
    private final String id;

    public EntityReference(String type, String id, SourceLoc location) {
        super(location);
        this.type = type;
        this.id = id;
    }

    public EntityReference(String type, String id) {
        this(type, id, SourceLoc.MISSING);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj instanceof EntityReference ref)  {
            return ref.id.equals(this.id) && ref.type.equals(this.type);
        }
        return false;
    }

    // FIXME: There will be a problem with escaped characters
    public String getReference() {
        return type + "::\"" + id + "\"";
    }

    @Override
    public String toString() {
        return getReference();
    }

    @Override
    public JsonElement toJson() {
        JsonObject contents = new JsonObject();
        // By default we keep all strings unescaped, so to keep things consistent we
        // need to unescaped before converting to Json to get the same representation
        contents.addProperty("type", StringEscapeUtils.unescapeJava(type));
        contents.addProperty("id", StringEscapeUtils.unescapeJava(id));
        JsonObject entity = new JsonObject();
        entity.add("__entity", contents);
        return entity;
    }

    public JsonElement toMinimalJson() {
        return toJson().getAsJsonObject().get("__entity");
    }
}
