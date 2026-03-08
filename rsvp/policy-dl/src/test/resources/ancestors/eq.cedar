// Children can talk only to parents
permit (
    principal is Child,
    action == Action::"Talk",
    resource is Parent
) when {
    principal.ancestor == resource
};

