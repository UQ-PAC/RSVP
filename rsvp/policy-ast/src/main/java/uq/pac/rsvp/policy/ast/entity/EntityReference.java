package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

import java.util.Objects;

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
}
