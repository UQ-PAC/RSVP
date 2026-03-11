permit (
    principal is File,
    action == Action::"act",
    resource is Directory
) when {
     principal in resource
};
