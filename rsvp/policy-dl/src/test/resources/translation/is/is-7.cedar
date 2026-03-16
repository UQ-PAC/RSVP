permit (
    principal is User,
    action,
    resource
) when {
    principal.name == "Bob" && File::"file" != resource
};

