# RSVP PhotoApp

## System description

PhotoApp is a small photo-sharing platform that allows users to upload and view their photos.
For the sake of simplicity photos are organised in albums. A user can create any number of 
albums and upload any number of photos but a single photo can be in a one album only. 

In addition to regular users PhotoApp has administrators that can do pretty much anything,
except for using the platform as regular users. I.e., administrators (should be) prohibited
from creating albums or uploading photos. Additionally, while users can choose to add other users
as friends, administrators are not allowed to do so.

For privacy purposes user albums have three levels of visibility:
+ _Public_ - visible to anyone (registered)
+ _Protected_ - visible to administrators, owner or owner's friends 
+ _Private_ - visible to administrators or owner only

## Cedar Entities

At the Cedar level this system components (Photos, Accounts etc.) can be represented as follows:

```
// A single photo
entity Photo = {
    // Album the photo belongs to
    album: Album,
    // Size in bytes
    size : Long,
    // Type, such as JPG, PNG, TIFF or similar
    type: String
};

// Photo visibility levels
entity Visibility enum [ 
    "Private", 
    "Protected", 
    "Public" 
];

// Roles of platform participants
entity Role enum [ 
    "User",  // Regular user
    "Admin"  // Administrator
];

// PhotoApp platform account including a person's name, role and friends
entity Account = {
    name: String,
    role: Role,
    friends: Set<Account>,
};

// Photo album
entity Album = {
    // The owner account of the album
    owner: Account,
    // Its visibility level
    visibility: Visibility
};
```

## Cedar Actions

The subset of relevant actions that can be performed on the platform

```
// An account views a given photo
action viewPhoto appliesTo {
    principal: Account,
    resource: Photo
};

// An account removes the album (belonging to the account)
action removeAlbum appliesTo {
    principal: Account,
    resource: Album
};

// An account uploads a photo to a given album
action uploadPhoto appliesTo {
    principal: Account,
    resource: Album
};

// An account creates an album for another account, e.g.,
// (User) Alice creates an album for herself or (Administrator) Tim
// creates an album for another user
action createAlbum appliesTo {
    principal: Account,
    resource: Account
};
```

## Cedar Security Policies

```
// === View
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    // + Anyone can see public photos
    resource.album.visibility == Visibility::"Public" ||   
    // + Owner can see their photos
    principal == resource.album.owner ||     
    // + Owner's friends can see protected photos
    resource.album.visibility == Visibility::"Protected" && principal in resource.album.owner.friends ||
    // + Admins can see any photos
    principal.role == Role::"Admin"
};

// === Upload
//  + Users can upload photos to their albums
//    (Implicitly admins cannot upload photos for themselves because they cannot create albums)
permit (
    principal is Account,
    action == Action::"uploadPhoto",
    resource is Album)
when {
    resource.owner == principal
};

// == Create
//  + users can create albums for themselves
//  + admins can create albums for anyone ...
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
//   + Admins can remove any albums
permit (
    principal is Account,
    action == Action::"removeAlbum",
    resource is Album)
when {
    resource.owner == principal || principal.role == Role::"Admin"
};
```

## Security invariants (security system boundaries)

One security requirement of the system is that administrators should be prohibited 
from uploading photos. Formally this restriction is implicit, i.e., security policies
prevend administrators from creating albums, as such no photos can be uploaded.
If a system is in a broken state, however, where an admin already has an album 
they are not prevented from uploading any number of photos.

The following invariant can be used to check whether this security requirement holds
in the given system. Namely, this ensures that there exists no system administrators 
who own albums.
```
@invariant("Administrators do not own photo albums")
album.owner.role == Role::"Admin" 
    for none album : Album;
```

Note that the above invariant evaluates the state of the system rather than look at what
policies allow. Another invariant stems from the requirement that admins should be able to 
access any photo regardless of its classification. The following invariant takes into account 
the state of the system as well as queries permissions given by the above security policies.

```
@invariant("Administrators can view any photo")
if (principal.role == Role::"Admin") then allow(principal, resource, Action::"viewPhoto") else true
    for all principal: Account, resource: Photo;
```

The next invariant ensures that users cannot create albums for anyone else but themselves.
In other words if one user can create an album for another user then this is the same user.

```
@invariant("UserRights")
!(bob.role == Role::"User" && alice.role == Role::"User" && allow(bob, alice, Action::"createAlbum")) || bob == alice
    for all bob: Account, alice: Account;
```

The final invariant ensures that the security requirement prohibiting administrators from creating
albums is enforced by the policies
```
@invariant("Administrators should not create albums")
tim.role == Role::"Admin" && anyone.role == Role::"Admin" --> deny(tim, anyone, Action::"createAlbum")
    for all tim: Account, anyone: Account;
```