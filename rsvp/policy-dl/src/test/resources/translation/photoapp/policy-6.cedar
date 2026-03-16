// === Remove
//   + Users can remove their albums
permit (
    principal is Account,
    action == Action::"removeAlbum",
    resource is Album)
when {
    resource.owner == principal
};

//   + Admins can remove any albums
permit (
    principal is Account,
    action == Action::"removeAlbum",
    resource is Album)
when {
    principal.role == Role::"Admin"
};