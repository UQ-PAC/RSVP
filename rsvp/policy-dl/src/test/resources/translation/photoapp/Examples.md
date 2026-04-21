# Examples of Analysing Photoapp application

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

