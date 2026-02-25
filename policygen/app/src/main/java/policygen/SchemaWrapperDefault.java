package policygen;

import java.util.Collection;
import java.util.List;

public class SchemaWrapperDefault implements SchemaWrapper {

    // Principals:
    //   User::"username"
    //   Group::"groupname"
    // (Users can be in a group, groups can be in a group, there should be no cycles)
    //
    // "User" attributes:
    //    accessLevel (0 - 9)
    //    age (generated as 15 - 99)
    //    (parent: group)
    //
    // "Group":
    //    (parent: group)
    //
    // Resources:
    //   File::"filename"
    //      attributes:
    //         requiredLevel (0-9)
    //         owner (->User)
    //         creator (->User)
    //   Folder::"foldername"
    //      attributes as per file
    //
    // Actions:
    //   Action::"read"
    //   Action::"update"
    //   Action::"remove"
    //
    // Requests (environment):
    // (appear in condition clauses as "context.xxx")
    //    srcSubnet (string)
    //    maintenanceMode (bool)

    private static CedarEntityRef userType = new CedarEntityRef("User",
            new CedarField("accessLevel", CedarPrimitive.LONG),
            new CedarField("age", CedarPrimitive.LONG));
    private static CedarEntityRef groupType = new CedarEntityRef("Group");
    private static CedarEntityRef fileType = new CedarEntityRef("File",
            new CedarField("requiredLevel", CedarPrimitive.LONG),
            new CedarField("owner", userType),
            new CedarField("creator", userType));
    private static CedarEntityRef folderType = new CedarEntityRef("Folder",
            new CedarField("requiredLevel", CedarPrimitive.LONG),
            new CedarField("owner", userType),
            new CedarField("creator", userType));

    private static CedarRecord requestType = new CedarRecord(
            new CedarField("srcSubnet", CedarPrimitive.STRING),
            new CedarField("maintenanceMode", CedarPrimitive.BOOL)
            );

    // private Collection<CedarEntityRef> principalTypes = List.of(userType);
    // private Collection<CedarEntityRef> resourceTypes = List.of(fileType, folderType);

    @Override
    public Collection<CedarEntityRef> getAncestorTypes(CedarEntityRef entityType) {
       if (entityType == userType) {
           return List.of(groupType);
       }
       if (entityType == groupType) {
           return List.of(groupType);
       }
       if (entityType == fileType) {
           return List.of(folderType);
       }
       if (entityType == folderType) {
           return List.of(folderType);
       }
       return List.of();
    }

    @Override
    public CedarEntityRef getPrincipalType() {
        return userType;
    }

    @Override
    public CedarEntityRef getResourceType() {
        return fileType;
    }

    @Override
    public CedarRecord getRequestType() {
        return requestType;
    }
}
