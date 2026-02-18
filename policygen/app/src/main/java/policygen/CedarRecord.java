package policygen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CedarRecord implements CedarType {

    private Map<String, CedarType> fieldsMap = new HashMap<>();

    public CedarRecord(CedarField ... cedarFields) {
        for (CedarField field : cedarFields) {
            fieldsMap.put(field.getName(), field.getType());
        }
    }

    @Override
    public TypeId getTypeId() {
        return CedarType.TypeId.RECORD;
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
