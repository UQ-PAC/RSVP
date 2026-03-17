permit (
    principal is Account,
    action,
    resource is Account)
when {
    !principal.friends.containsAny([Account::"Alice", Account::"Bob"])
};

