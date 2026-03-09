package policygen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SchemaWrapperImpl implements SchemaWrapper {

    List<CedarEntityRef> entityTypes = new ArrayList<>();
    List<CedarAction> actions = new ArrayList<>();

    public SchemaWrapperImpl(Collection<CedarEntityRef> entityTypes, Collection<CedarAction> actions) {
        this.entityTypes.addAll(entityTypes);
        this.actions.addAll(actions);
    }

    @Override
    public CedarEntityRef getPrincipalType() {
        // FIXME this is a hack
        return actions.getFirst().getApplicablePrincipals().getFirst();
    }

    @Override
    public CedarEntityRef getResourceType() {
        // FIXME this is a hack
        return actions.getFirst().getApplicableResources().getFirst();
    }

    @Override
    public List<CedarAction> getActions() {
        return actions;
    }

    @Override
    public CedarRecord getRequestType() {
        // FIXME this is a hack
        return actions.getFirst().getContextType();
    }
}
