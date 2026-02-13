package policygen;

import java.util.Collections;
import java.util.Map;

public class CedarRecord implements CedarType {

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
        // TODO fix
        return Collections.emptyMap();
    }
}
