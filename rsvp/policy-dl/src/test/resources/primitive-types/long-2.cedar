permit (
    principal is User,
    action == Action::"Use",
    resource is File
) when {
    resource.size > 1 && principal.name == "Bob"

};

