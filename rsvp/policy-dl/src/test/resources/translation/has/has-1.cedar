permit (
    principal in Directory::"/usr/bin",
    action in Action::"manage",
    resource is Directory
) when {
    principal has "kind" && principal.kind == "executable"
};
