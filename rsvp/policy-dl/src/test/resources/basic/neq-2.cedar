// Children can talk to anyone but their parents (excluding ???)
// Because attributes of both principals and resources are used
permit (
    principal is Child,
    action == Action::"Talk",
    resource is Parent
) when {
    principal.ancestor != resource && resource.descendant != principal
};

