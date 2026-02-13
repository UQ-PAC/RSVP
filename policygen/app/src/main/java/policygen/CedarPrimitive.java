package policygen;

import java.util.Map;

public class CedarPrimitive implements CedarType {

    private TypeId typeId;

    public final static CedarPrimitive BOOL = new CedarPrimitive(TypeId.BOOL);
    public final static CedarPrimitive LONG = new CedarPrimitive(TypeId.LONG);
    public final static CedarPrimitive STRING = new CedarPrimitive(TypeId.STRING);

    private CedarPrimitive(TypeId typeId ) {
        this.typeId = typeId;
    }

    @Override
    public CedarType getElementType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, CedarType> getFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeId getTypeId() {
        return typeId;
    }

}
