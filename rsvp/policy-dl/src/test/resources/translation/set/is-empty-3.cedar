permit (
    principal is Account,
    action,
    resource is Account)
when {
    principal.permissions.roles.isEmpty()
};

