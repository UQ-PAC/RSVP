// Children can talk to anyone
permit (
    principal is Child,
    action == Action::"Talk",
    resource is Parent
);

