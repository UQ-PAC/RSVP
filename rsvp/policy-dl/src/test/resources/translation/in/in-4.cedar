permit (
    principal == File::"ls",
    action == Action::"act",
    resource is Directory
) when {
    // ls in sda, true
    principal in Partition::"sda"
};
