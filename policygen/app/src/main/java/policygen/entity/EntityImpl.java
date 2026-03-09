package policygen.entity;

public class EntityImpl extends Entity {

    private String entityType;
    private String entityId;

    public EntityImpl(String entityType, String entityId) {
        if (entityType == null || entityId == null) {
            throw new NullPointerException();
        }

        this.entityType = entityType;
        this.entityId = entityId;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    @Override
    public String getEntityType() {
        return entityType;
    }
}
