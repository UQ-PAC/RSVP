package policygen;

import java.util.Collection;

public interface SchemaWrapper {

    /**
     * Given an entity type, get all the entity types that can be an ancestor of the type.
     */
    Collection<CedarEntityRef> getAncestorTypes(CedarEntityRef entityType);

    /**
     * Get the entity type for principals
     * TODO multiple types?
     */
    CedarEntityRef getPrincipalType();

    /**
     * Get the entity type for resources
     * TODO multiple types?
     */
    CedarEntityRef getResourceType();

    /**
     * Get the record type corresponding to request contexts
     */
    public CedarRecord getRequestType();

}
