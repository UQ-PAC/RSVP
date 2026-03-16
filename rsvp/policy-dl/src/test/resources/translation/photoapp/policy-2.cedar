// === View
//   + Owner's friends can see protected photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Protected"
        && principal in resource.album.owner.friends
};
