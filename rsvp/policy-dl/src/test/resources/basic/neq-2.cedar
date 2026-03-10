permit (
    principal is Child,
    action == Action::"Talk",
    resource is Parent
) when {
    principal.ancestor != resource && resource.descendant != principal
};

