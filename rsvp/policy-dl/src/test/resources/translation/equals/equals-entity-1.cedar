// == over entities
permit (
    principal is File,
    action == Action::"introspect",
    resource is File
) when {
    principal == resource
};
