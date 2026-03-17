permit (
    principal is Partition,
    action in Action::"lookup",
    resource is Directory
) when {
    principal has "directories"
};
