permit (
    principal,
    action,
    resource
) when {
    principal has "role" && principal.role == Role::"Admin"
};
