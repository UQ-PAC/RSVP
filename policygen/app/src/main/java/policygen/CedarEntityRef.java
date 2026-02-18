package policygen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CedarEntityRef implements CedarType {

    private String entityType;
    private Map<String, CedarType> fieldsMap = new HashMap<>();

    public CedarEntityRef(String entityType, CedarField ... cedarFields) {
        this.entityType = entityType;
        for (CedarField field : cedarFields) {
            fieldsMap.put(field.getName(), field.getType());
        }
    }

    public String getEntityType() {
        return entityType;
    }

    @Override
    public TypeId getTypeId() {
        return CedarType.TypeId.ENTITY;
    }

    @Override
    public CedarType getElementType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, CedarType> getFields() {
        return Collections.unmodifiableMap(fieldsMap);
    }
}
