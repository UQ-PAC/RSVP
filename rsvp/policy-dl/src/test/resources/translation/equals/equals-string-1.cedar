// == over strings (vars)
permit (
    principal is File,
    action == Action::"introspect",
    resource is BlkDev
) when {
    principal.attributes.ext == resource.prop.name.val
};
