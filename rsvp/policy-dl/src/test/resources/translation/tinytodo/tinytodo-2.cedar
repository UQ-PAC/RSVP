// Policy 1: A User can perform any action on a List they own
permit (
    principal,
    action,
    resource)
when {
    resource has owner && resource.owner == principal
};

