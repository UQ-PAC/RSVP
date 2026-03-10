// Children can talk to anyone
permit (
    principal,
    action == Action::"Talk",
    resource is Parent
);

