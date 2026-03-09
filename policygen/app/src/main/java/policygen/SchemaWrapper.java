package policygen;

import java.util.List;

public interface SchemaWrapper {

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
     * Get the list of actions (with details such as their applicable resource/principal types)
     */
    List<CedarAction> getActions();

    /**
     * Get the record type corresponding to request contexts
     */
    public CedarRecord getRequestType();

}
