permit (
    principal == File::"ls",
    action == Action::"act",
    resource is Directory
) when {
    // Always false for ls, it is not a member of the hierarchy
    principal in Directory::"/usr/sbin"
};
