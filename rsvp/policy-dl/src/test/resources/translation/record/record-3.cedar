permit (
    principal,
    action == Action::"locate",
    resource
) when {
    principal.prop.name.val == "loop"
};
