package policygen;

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

    private static CedarEntityRef groupType;
    private static CedarEntityRef userType;
    private static CedarEntityRef folderType;
    private static CedarEntityRef fileType;

    private static CedarRecord requestType = new CedarRecord(
            new CedarField("srcSubnet", CedarPrimitive.STRING),
            new CedarField("maintenanceMode", CedarPrimitive.BOOL)
            );

    private static List<CedarAction> actions;

    static {
        groupType = new CedarEntityRef("Group");
        groupType.setParentTypes(List.of(groupType));
        userType = new CedarEntityRef("User",
                List.of(groupType),
                new CedarField("accessLevel", CedarPrimitive.LONG),
                new CedarField("age", CedarPrimitive.LONG));
        folderType = new CedarEntityRef("Folder",
                new CedarField("requiredLevel", CedarPrimitive.LONG),
                new CedarField("owner", userType),
                new CedarField("creator", userType));
        folderType.setParentTypes(List.of(folderType));
        fileType = new CedarEntityRef("File",
                List.of(folderType),
                new CedarField("requiredLevel", CedarPrimitive.LONG),
                new CedarField("owner", userType),
                new CedarField("creator", userType));

        actions = List.of(
                new CedarAction("Action::\"read\"", List.of(userType), List.of(fileType), requestType),
                new CedarAction("Action::\"update\"", List.of(userType), List.of(fileType), requestType),
                new CedarAction("Action::\"remove\"", List.of(userType), List.of(fileType), requestType));
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

    @Override
    public List<CedarAction> getActions() {
        return actions;
    }
}
