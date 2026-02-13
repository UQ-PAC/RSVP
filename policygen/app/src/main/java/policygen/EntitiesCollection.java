package policygen;

import java.util.List;

import policygen.entity.Entity;

public class EntitiesCollection {

    // TODO handle parent types

    private CedarType entityType;

    private List<? extends Entity> entities;

    public EntitiesCollection(CedarType entityType, List<? extends Entity> entities) {
        this.entityType = entityType;
        this.entities = entities;
    }

    public CedarType getEntityType() {
        return entityType;
    }

    public List<? extends Entity> getEntities() {
        return entities;
    }
}
