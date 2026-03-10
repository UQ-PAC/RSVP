permit (
    principal is User,
    action == Action::"Use",
    resource is File
) when {
    resource.kind == "file"
};

