permit (
    principal,
    action,
    resource
) when {
    principal.role == Role::"Important User"
};
