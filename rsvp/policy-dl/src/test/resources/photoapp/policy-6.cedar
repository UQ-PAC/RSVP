// === Remove
//   + Users can remove their albums
//   + Admins can remove any albums
@name("permitRemoveAlbum")
permit (
    principal is Account,
    action == Action::"removeAlbum",
    resource is Album)
when {
    resource.owner == principal || principal.role == Role::"Admin"
};