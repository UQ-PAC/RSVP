package policygen.entity;

public abstract class Entity {
    public Entity parent;

    public abstract String getEntityId();

    public String toEntityString() {
        return getEntityType() + "::\"" + getEntityId() + "\"";
    }

    public String getEntityType() {
        // FIXME this is awful
        return getClass().getSimpleName();
    }
}
