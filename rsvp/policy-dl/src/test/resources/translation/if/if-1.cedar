permit (
    principal,
    action,
    resource
) when {
    if principal.kind == Kind::"Folder" then
        principal.permission.exec
    else
        principal.permission.read && principal.permission.write
};
