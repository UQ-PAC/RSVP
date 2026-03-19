package policygen;

import java.util.Map;

public class CedarSet implements CedarType {

    private CedarType elementType;

    public CedarSet(CedarType elementType) {
        this.elementType = elementType;
    }

    @Override
    public CedarType getElementType() {
        return elementType;
    }

    @Override
    public Map<String, CedarType> getFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeId getTypeId() {
        return TypeId.SET;
    }
}
