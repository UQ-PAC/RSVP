// If enums are not mentioned in the entities it is ok,
// Enums are EID only and only relevant for validation
permit (
    principal,
    action,
    resource
) when {
    principal.role == Role::"Admin"
};
