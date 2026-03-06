// === Upload
//  + Users can upload photos to their albums
//     (Implicitly admins cannot upload photos for
//      themselves because they cannot create albums)
@name("permitUploadPhoto")
permit (
    principal is Account,
    action == Action::"uploadPhoto",
    resource is Album)
when {
    resource.owner == principal
};