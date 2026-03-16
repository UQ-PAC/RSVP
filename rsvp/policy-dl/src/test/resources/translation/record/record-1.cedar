permit (
    principal,
    action == Action::"introspect",
    resource
) when {
    principal.attributes.location.partition == Partition::"SDA"
};
