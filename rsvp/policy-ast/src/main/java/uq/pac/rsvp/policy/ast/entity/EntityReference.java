package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Arrays;
import java.util.List;
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

    // FIXME: Remove
    public AntlrTypeReference getSchemaReference() {
        List<String> parts = Arrays.asList(type.split("::"));
        String namespace = String.join("::", parts.subList(0, parts.size() - 1));
        return new AntlrTypeReference(namespace, parts.getLast());
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
}
