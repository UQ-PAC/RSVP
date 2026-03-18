permit (
    principal is Person,
    action,
    resource is Person)
when {
    !principal.traits.containsAny(resource.traits)
};

