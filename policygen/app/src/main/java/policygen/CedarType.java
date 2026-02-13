package policygen;

import java.util.Map;

public interface CedarType {

    public static enum TypeId {
        BOOL, STRING, LONG, SET, RECORD,
        ENTITY, // Entity reference,
        EXT, // Extensions eg datetime("..."), decimal("..."), duration("..."), ipaddr("...")
    }

    /**
     * Get the type classification identifier.
     */
    TypeId getTypeId();

    /**
     * Get the element type - valid for Set types. May return null for set of any.
     */
    CedarType getElementType();

    /**
     * Get field names and types - valid for Record types.
     */
    Map<String, CedarType> getFields();
}
