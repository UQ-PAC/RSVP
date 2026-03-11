permit (
    principal == File::"ls",
    action == Action::"act",
    resource is Directory
) when {
    // ls is not in sdb, false
    principal in Partition::"sdb"
};
