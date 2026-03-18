permit (
    principal is Person,
    action,
    resource is Person)
when {
    principal.traits.containsAll([resource.trait])
};

