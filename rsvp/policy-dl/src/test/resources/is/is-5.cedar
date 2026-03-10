permit (
    principal is User,
    action,
    resource
) when {
    principal.name == "Bob" &&
        resource == File::"file" &&
        resource.kind == "file"
};

