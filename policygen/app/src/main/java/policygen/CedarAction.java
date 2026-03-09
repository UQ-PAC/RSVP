package policygen;

import java.util.ArrayList;
import java.util.List;

public class CedarAction {

    private String name;
    private List<CedarEntityRef> applicablePrincipals = new ArrayList<>();
    private List<CedarEntityRef> applicableResources = new ArrayList<>();
    private CedarRecord contextType; // may be null

    public CedarAction(String name, List<CedarEntityRef> principals, List<CedarEntityRef> resources, CedarRecord contextType) {
        this.name = name;
        this.applicablePrincipals = principals;
        this.applicableResources = resources;
        this.contextType = contextType;
    }

    public String getName() {
        return name;
    }

    public List<CedarEntityRef> getApplicablePrincipals() {
        return applicablePrincipals;
    }

    public List<CedarEntityRef> getApplicableResources() {
        return applicableResources;
    }

    public CedarRecord getContextType() {
        return contextType;
    }
}
