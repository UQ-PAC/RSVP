permit (
    principal,
    action == Action::"locate",
    resource
) when {
    principal.prop.name.val == "null"
};
