// Children can talk to anyone but their parents (including ???)
// But principals cannot have undefined, resources
permit (
    principal is Child,
    action == Action::"Talk",
    resource is Parent
) when {
    principal.ancestor != resource
};

