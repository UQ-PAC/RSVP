permit (
    principal is User,
    action == Action::"Use",
    resource is File
) when {
    resource.read && resource.write && resource.kind == "other"
};

