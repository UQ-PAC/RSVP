// === View
//   + Anyone can see public photos
//   + Users can see their own photos
//   + Admins can see all photos
@permitViewPhoto
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
        || principal.role == Role::"Admin"
        || resource.album.owner == principal
};

// === View
//   + Owner's friends can see protected photos
@permitViewProtedtedPhoto("unsupported")
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Protected"
        && principal in resource.album.owner.friends
};

// === Upload
//  + Users can upload photos to their albums
//     (Implicitly admins cannot upload photos for
//      themselves because they cannot create albums)
@permitUploadPhoto
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
@permitCreateAlbum
permit (
    principal is Account,
    action == Action::"createAlbum",
    resource is Account)
when {
    resource == principal || principal.role == Role::"Admin"
};

@forbidCreateAlbum
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
//   + Admins can remove any albums
@permitRemoveAlbum
permit (
    principal is Account,
    action == Action::"removeAlbum",
    resource is Album)
when {
    resource.owner == principal || principal.role == Role::"Admin"
};
