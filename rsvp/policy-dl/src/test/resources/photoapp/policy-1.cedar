// === View
//   + Anyone can see public photos
//   + Users can see their own photos
//   + Admins can see all photos
@name("permitViewPhoto")
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
        || principal.role == Role::"Admin"
        || resource.album.owner == principal
};