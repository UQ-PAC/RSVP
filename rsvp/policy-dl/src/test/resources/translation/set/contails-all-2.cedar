permit (
    principal is Account,
    action,
    resource is Account)
when {
    !principal.friends.containsAll([Account::"Alice", Account::"Bob"])
};

