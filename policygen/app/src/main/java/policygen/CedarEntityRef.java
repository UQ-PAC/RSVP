package policygen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CedarEntityRef implements CedarType {

    private String entityType;
    private Map<String, CedarType> fieldsMap = new HashMap<>();
    private List<CedarEntityRef> parentTypes = new ArrayList<>();

    public CedarEntityRef(String entityType, CedarField ... cedarFields) {
        this.entityType = entityType;
        setFields(cedarFields);
    }

    public CedarEntityRef(String entityType, Collection<CedarEntityRef> parentTypes, CedarField ... cedarFields) {
        this.entityType = entityType;
        this.parentTypes.addAll(parentTypes);
        setFields(cedarFields);
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

    public void setFields(CedarField[] cedarFields) {
        for (CedarField field : cedarFields) {
            fieldsMap.put(field.getName(), field.getType());
        }
    }

    public void setParentTypes(List<CedarEntityRef> parentTypes) {
        this.parentTypes = parentTypes;
    }

    public List<CedarEntityRef> getParentTypes() {
        return parentTypes;
    }
}
