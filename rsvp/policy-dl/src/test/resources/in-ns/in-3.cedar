permit (
    principal is OS::File,
    action == OS::Action::"act",
    resource is OS::Directory
) when {
     principal in OS::Partition::"sda" && !(principal in resource)
};
