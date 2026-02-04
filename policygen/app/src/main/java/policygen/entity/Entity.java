package policygen.entity;

public abstract class Entity {
    public Entity parent;

    public abstract String getEntityId();

    public String toEntityString() {
        return getClass().getSimpleName() + "::\"" + getEntityId() + "\"";
    }
}
