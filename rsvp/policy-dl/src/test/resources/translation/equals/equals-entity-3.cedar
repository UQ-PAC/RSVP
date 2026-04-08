// == over entities (two literals)
permit (
    principal is File,
    action == Action::"introspect",
    resource is File
) when {
    File::"mkdir" == File::"ls"
};
