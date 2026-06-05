permit (
    principal is Numeric,
    action,
    resource is Numeric)
when {
    (principal.a && principal.b) == (resource.a && resource.b)
};
