// == over numbers (var,literal)
permit (
    principal is File,
    action == Action::"introspect",
    resource is BlkDev
) when {
    5 == 5
};
