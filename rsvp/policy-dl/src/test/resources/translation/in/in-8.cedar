permit (
    principal,
    action == Action::"act",
    resource is Directory
) when {
    principal in [Directory::"sbin", Directory::"bin"]
};
