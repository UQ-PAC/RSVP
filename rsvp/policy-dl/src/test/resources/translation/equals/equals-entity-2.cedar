// == over entities (one literal)
permit (
    principal is File,
    action == Action::"introspect",
    resource is File
) when {
    principal == File::"ls"
};
