// === View
//   + Anyone can see public photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
};

//   + Owner's friends can see protected photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Protected"
        && principal in resource.album.owner.friends
};

//   + Admins can see any photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    principal.role == Role::"Admin"
};

// === Upload
//  + Users can upload photos to their albums
//     (Implicitly admins cannot upload photos for
//      themselves because they cannot create albums)
permit (
    principal is Account,
    action == Action::"uploadPhoto",
    resource is Album)
when {
    resource.owner == principal
};
// == Create
//  + users can create albums for their own accounts
//  + admins can create albums for anyone
permit (
    principal is Account,
    action == Action::"createAlbum",
    resource is Account)
when {
    resource == principal || principal.role == Role::"Admin"
};
//   + ... except themselves or other admins
forbid (
    principal is Account,
    action == Action::"createAlbum",
    resource is Account)
when {
    resource.role == Role::"Admin"
};
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