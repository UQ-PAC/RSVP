//   + Anyone can see public photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
};

//   + Admins can see all photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    principal.role == Role::"Admin"
};

//   + Users can see their own photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.owner == principal
};
