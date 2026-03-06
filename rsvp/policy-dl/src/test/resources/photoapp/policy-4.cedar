// == Create
//  + users can create albums for their own accounts
//  + admins can create albums for anyone
@name("permitCreateAlbum")
permit (
    principal is Account,
    action == Action::"createAlbum",
    resource is Account)
when {
    resource == principal || principal.role == Role::"Admin"
};