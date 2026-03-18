permit (
    principal is Person,
    action,
    resource is Person)
when {
    !principal.traits.contains(resource.trait)
};

