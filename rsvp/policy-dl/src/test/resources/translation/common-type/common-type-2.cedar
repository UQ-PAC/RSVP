permit (
    principal is File,
    action == Action::"introspect",
    resource is Directory
) when {
    principal.access == resource.access
};
