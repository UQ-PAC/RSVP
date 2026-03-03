permit (
    principal is User in Group::"Cats",
    action == Action::"View",
    resource is Photo
) when {
    resource in principal
};

permit (
    principal,
    action,
    resource is Photo
) when {
    principal has company && principal.company == "pets"
};

