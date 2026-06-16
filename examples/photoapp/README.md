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
invariant album.owner.role == Role::"Admin"
    for none album : Album;
```

Note that the above invariant evaluates the state of the system rather than look at what
policies allow. Another invariant stems from the requirement that admins should be able to
access any photo regardless of its classification. The following invariant takes into account
the state of the system as well as queries permissions given by the above security policies.

```
invariant
if (principal.role == Role::"Admin") then allow(principal, resource, Action::"viewPhoto") else true
    for all principal: Account, resource: Photo;
```

The next invariant ensures that users cannot create albums for anyone else but themselves.
In other words if one user can create an album for another user then this is the same user.

```
invariant
!(bob.role == Role::"User" && alice.role == Role::"User" && allow(bob, alice, Action::"createAlbum")) || bob == alice
    for all bob: Account, alice: Account;
```

The final invariant ensures that the security requirement prohibiting administrators from creating
albums is enforced by the policies
```
invariant
tim.role == Role::"Admin" && anyone.role == Role::"Admin" --> deny(tim, anyone, Action::"createAlbum")
    for all tim: Account, anyone: Account;
```

## Examples of analysing Photoapp application

Here we provide examples of policies with respect to the [existing policy set of
photoapp](photoapp.cedar) and its [schema](photoapp.cedarschema).

## 1. Detect redundant, irrelevant or correlated policies

#### Irrelevance

An _irrelevant_ policy is a policy that has does not affect permissiveness.
Typically, an irrelevant policy features an impossible condition.

For instance, consider a policy that disallows viewing a photo if its type is undefined
(by checking whether a type attribute is present). However, since the type
attribute is ensured to exist by the schema, the condition always evaluates to false
and as a result this policy has no effect in that it forbids nothing.

```
forbid (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    !(resource has type)
};
```

#### Redundancy

A _redundant_ policy is a policy whose permissions are subsumed by other policy.
Removing a redundant policy has no effect on permissiveness of a policy set.

Consider, for instance the following policy that allows users to view public photos.
```
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    principal.role == Role::"User" && resource.album.visibility == Visibility::"Public"
};
```

Considering that another policy that allows all accounts to view public photos exist, the above
policy is redundant.

#### Correlation

Policies _P_ and _Q_ are considered _correlated_ if _P_ allows (resp. forbids) some of the requests
that _Q_ allows (resp. forbids) and vice versa. The significance of policy correlation is that if
one wants to fully eliminate the effects of _P_ it is also necessary to modify all policies that
_P_ is correlated with.

Consider the following policies

```
//   + Anyone can see public photos (P)
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
};

//   + Admins can see any photos (Q)
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    principal.role == Role::"Admin"
}
```

In the example above the policies are correlated in that admins will have the rights to see public
photos regardless whether the _P_ is in effect or not.

Since RSVP views policies and their effects as request sets, _redundant_, _irrelevant_ or _correlated_
policies can be identified.

## Resource/action coverage

Consider a scenario where one extends the application with new functionality allowing new actions
to take place. For instance, an action allowing admins to delete user accounts:

```
action removeAccount appliesTo {
    principal: Account,
    resource: Account
};
```

However, since no policies allowing account removal exist our analysis can identify that
this particular action is not covered by the policies.
Note, that because we use closed-world encoding the analysis can distinguish between

+ A case where no policies involving a particular action have been defined in the policy set
+ A case where policies exist, but disallow actions of a given type

In other words RSVP can determine whether policies for a given action exist or they are
deliberately restrictive.

Similar analysis can be applied to entity types (e.g., check whether there are policies
involving a particular entity type) or even to specific entities.

## Conflicts within policy sets

Within a policy set Cedar combines results of multiple policies to arrive at a decision.
A request is permitted only if it is explicitly permitted and not explicitly denied.

However, an application deployed in different Cloud environments may have different security
requirements and different policies, and it can be beneficial to understand these
differences. RSVP can analyse and identify conflicts between different policy sets.
In this context, a _conflict_ refers to a request permitted by one policy set and denied by another.

By design, the Photoapp application prohibits administrator accounts to use the platform as regular
users. For instance, administrators are forbidden from creating albums via the following policy:
```
//   + Admins cannot create albums for themselves or other admins
forbid (
    principal is Account,
    action == Action::"createAlbum",
    resource is Account)
when {
    resource.role == Role::"Admin"
};
```
Consider deployments D1 and D2, such that D1 has the above restriction and D2 does not.
RSVP can analyse two policy sets and pinpoint request outcomes that differ as well as the
offending policies. In this instance RSVP analysis can identify conflicting requests from
D1 and D2 and pinpoint the offending (above) policy as the reason behind conflicts.

## Change impact analysis

One key feature of RSVP analysis is that it can quantify and reason about
changes in permissiveness due to changes in policies.

Consider the following policy:
```
// Allow anyone to see public photos
// Admins can see any photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    principal.role == Role::"Admin" || resource.album.visibility == Visibility::"Public"
};
```

and requests
```
(1) { principal: 'Account::"Alice"', resource: 'Photo::"Meadow.jpg"', action: 'Action::"viewPhoto"' }
(2) { principal: 'Account::"Tim"', resource: 'Photo::"Meadow.jpg"', action: 'Action::"viewPhoto"' }
```

Since anyone can see public photo then both user `Alice` and administrator `Tim`
are allowed to view public photo `Meadow.jpg`.

Now consider a slight change that forbids anyone to see public photos as follows:
```
// Allow anyone to see public photos
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    principal.role == Role::"Admin"
};
```

The effect of this change is that request (1) is now denied and
this change of request status can be reported as a change in permissiveness.

