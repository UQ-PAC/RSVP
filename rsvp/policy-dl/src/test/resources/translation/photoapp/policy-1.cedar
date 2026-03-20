//   + Anyone can see public photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
};

