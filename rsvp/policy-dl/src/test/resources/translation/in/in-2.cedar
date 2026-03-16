permit (
    principal == File::"ls",
    action == Action::"act",
    resource is Directory
) when {
    // Always true for ls, it is a member of the hierarchy
    principal in Directory::"/usr/bin"
};
