// == over numbers (vars)
permit (
    principal is File,
    action == Action::"introspect",
    resource is BlkDev
) when {
    principal.attributes.permission.read == resource.prop.name.read
};
