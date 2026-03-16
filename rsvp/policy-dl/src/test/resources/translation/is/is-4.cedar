permit (
    principal is User,
    action,
    resource
) when {
    principal.name == "Bob" && !(resource is Socket)
};

