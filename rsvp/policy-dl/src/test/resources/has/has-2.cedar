permit (
    principal in Directory::"/usr/bin",
    action in Action::"manage",
    resource is Directory
) when {
    principal has "size" && principal is File && principal.size > 100
};
