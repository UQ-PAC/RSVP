permit (
    principal == File::"ls",
    action == Action::"act",
    resource is Directory
) when {
    // in is reflexive, an element is a member of its own hierarchy
    principal in File::"ls"
};
