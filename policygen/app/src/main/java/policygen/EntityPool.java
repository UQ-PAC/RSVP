package policygen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import policygen.entity.Entity;

public class EntityPool {

    private Map<String, List<Entity>> entitiesMap = new HashMap<>();

    public void addEntity(Entity e) {
        String entityType = e.getEntityType();
        List<Entity> entitiesOfType = entitiesMap.computeIfAbsent(entityType,
                key -> new ArrayList<Entity>());
        entitiesOfType.add(e);
    }

    public Entity getRandomEntityOfType(CedarEntityRef entityType, Random rng) {
        //System.out.println("Available entity types: " + entitiesMap);
        //System.out.println("Get entity of type: " + entityType.getEntityType());
        List<Entity> entitiesOfType = entitiesMap.get(entityType.getEntityType());
        if (entitiesOfType == null)
            return null;

        return entitiesOfType.get(rng.nextInt(entitiesOfType.size()));
    }

}
