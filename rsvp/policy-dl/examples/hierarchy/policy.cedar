permit (
    principal is User in Group::"Cats",
    action == Action::"View",
    resource is Photo
) when {
    resource in principal
};
