// == over strings (literals)
permit (
    principal is File,
    action == Action::"introspect",
    resource is BlkDev
) when {
    "principal.ext" == "resource.name.val"
};
