permit (
    principal is User,
    action == Action::"Use",
    resource is File
) when {
    resource.size < 2 && principal.name == "Bob"
};

