permit (
    principal is Account,
    action == Action::"removeAlbum",
    resource is Album)
when {
    principal.role == Role::"Admin"
};