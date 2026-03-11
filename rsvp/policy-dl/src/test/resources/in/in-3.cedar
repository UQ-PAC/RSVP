permit (
    principal is File,
    action == Action::"act",
    resource is Directory
) when {
     principal in Partition::"sda" &&
         !(principal in resource)
};
