# Datalog (Soufflé) Encoding of Cedar Policies

## Closed-world Assumption

For this type of analysis we assume operating in a closed world with a 
finite set of entities. Consequently, facts and relations not given by entities,
schema or policies are treated as _false_ rather than _undefined_. 
Under this assumption the precision of the analysis is limited by the world,
however, the key intent is to reason about the model of the system
(even if incomplete) and reduce the potential search space.

## Cedar Extension for Invariant Checking

A permit or forbid rule of Cedar policy language can be written as 
a boolean-valued expression over variables _principal_, _action_ and _resource_. 
A Cedar policy (say _P_), then can be written as `(A1 ∧ ... ∧ An) ∧ ¬(F1 ∨ ... ∨ Fk)`,
where `<A1, ..., An>` and  `<F1, ... Fk>` are permit and forbid rules respectively.
An input request _R_ (as a value triple `(a, p, r)`) is permitted
as long as the instantiation of _P_ (wrt a given entity set) with 
the values of _R_ evaluates to _true_ and forbidden otherwise. 
The request _R_ can be seen as an additional constraint 
`principal == p ∧ action == a ∧ resource == r`.

For a more general case we let Cedar expressions range over a set of arbitrary variables
as long as their types are known and introduce the notion of quantification. 

```
    (1) <expr> for all  p : Account, a : Action
    (2) <expr> for some p : Account, a : Action
```

The above example shows universal (1) and existential (2) quantification for some Cedar expression
`<expr>` over variables `p` of type _Account_ and `a` of type `Action`. (1) holds if for any
combination of `a` and `p` <expr> evaluates to _true_ (wrt some set of entities). Respectively,
(2) holds as long as there exists at least one pair `(a, p)` for which `<expr>` is _true_.

## Encoding inputs

For correct encoding of Cedar policies we assume a fixed set of entities (representing the world),
a Cedar policy and its corresponding schema. Before encoding, the provided set of entities and 
the policy are assumed are validated against the schema.

## Encoding

A complete example of Datalog encoding using policies, schema an entities of an example application
+ [Schema](./photoapp.cedarschema) 
+ [Policies](./photoapp-policy.cedar)
+ [Entities](./entities.json)
+ [Resulting encoding using Souffle Datalog](./photoapp-auth.dl)

## (Step 1) Entity definitions

Each type in the schema and a set of accompanying entities are encoded as unary relations that
allow to restrict the world (including actions). Example conversion is as follows.

```
entity Role;
entity Account = {
    "name": String,
    "age": Long,
    "role": Role,
    "friends": Set<Account>,
    "albums": Set<Album>
};
```

```datalog
.type RoleType <: symbol
.decl Role(role: RoleType)
Role("Role::User").
Role("Role::Admin").

.type AccountType <: symbol
.decl Account(id: AccountType)
Account("Account::Alice").
Account("Account::Bob").
Account("Account::Carl").
Account("Account::Tim").
```

Given that each entity is uniquely identified as its namespace, type and id, each corresponding
Datalog type is derived from _symbol_ primitive type.


## (Step 2) Encoding relations

Relations between entities and their contents are modelled as binary relations (over respective types).
For instance, the following _Account_-type entities have properties _age_ and _friends_
that can be encoded as follows.

```    
{
        "uid": { "type": "Account", "id": "Alice" },
        "attrs": {
            "age" : 22,
            "role" : { "type": "Role", "id": "User" },
            "friends" : [
                { "type": "Account", "id": "Bob" }
            ],
        },
        "parents": []
    },
    {
        "uid": { "type": "Account", "id": "Bob" },
        "attrs": {
            "name" : "Bob",
            "age" : 25,
            "role" : { "type": "Role", "id": "User" },
            "friends" : [
                { "type": "Account", "id": "Alice" },
                { "type": "Account", "id": "Carl" }
            ],
        },
        "parents": []
    },
    {
        "uid": { "type": "Account", "id": "Carl" },
        "attrs": {
            "name" : "Carl",
            "age" : 27,
            "role" : { "type": "Role", "id": "User" },
            "friends" : [
                { "type": "Account", "id": "Bob" }
            ],
        },
        "parents": []
    }
```

```Datalog
.decl AccountAge(accout: AccountType, age: number)
AccountAge("Account::Alice", 22).
AccountAge("Account::Bob",   25).
AccountAge("Account::Carl",  27).
AccountAge("Account::Tim",   34).

.decl AccountFriends(account: AccountType, friend: AccountType)
AccountFriends("Account::Alice", "Account::Bob").
AccountFriends("Account::Bob",   "Account::Alice").
AccountFriends("Account::Bob",   "Account::Carl").
AccountFriends("Account::Carl",  "Account::Bob").
```

## (Step 3) Policy Conversion

Each Cedar-level rule (permit or forbid) is translated into a unique relation
(over actions, principals and resources) using relations defined before. 
For instance, a permit rule 
```permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
};
```

is encoded as follows:
```Datalog
.decl PermitV1(action: ActionType, principal: PrincipalType, resource: ResourceType)
// Anyone can see public photos
PermitV1(action, principal, resource) :-
    action = "Action::viewPhoto",
    Account(principal),
    Photo(resource),
    PhotoInAlbum(resource, album),
    AlbumVisibility(album, "Visibility::Public").
```

## (Step 4) Request rules

The following relations are added.
```
// All permitted requests
.decl Permit(action: ActionType, principal: PrincipalType, resource: ResourceType)
Permit(action, principal, resource) :- PermitV1(action, principal, resource).
Permit(action, principal, resource) :- PermitV2(action, principal, resource).
Permit(action, principal, resource) :- PermitU1(action, principal, resource).
Permit(action, principal, resource) :- PermitC1(action, principal, resource).
Permit(action, principal, resource) :- PermitR1(action, principal, resource).

// Requests explicitly forbidden by the policy
.decl Forbid(action: ActionType, principal: PrincipalType, resource: ResourceType)
Forbid(action, principal, resource) :-
    ForbidC1(action, principal, resource).

// All possible requests
.decl AllRequests(action: ActionType, principal: PrincipalType, resource: ResourceType)
AllRequests(action, principal, resource) :-
    ActionResource(action, resource), ActionPrincipal(action, principal).

// Requests allowed by the policy
.decl PermittedRequests(action: ActionType, principal: PrincipalType, resource: ResourceType)
PermittedRequests(action, principal, resource) :-
    Permit(action, principal, resource), !Forbid(action, principal, resource).

// Requests forbidden by the policy
.decl ForbiddenRequests(action: ActionType, principal: PrincipalType, resource: ResourceType)
ForbiddenRequests(action, principal, resource) :-
    AllRequests(action, principal, resource), !PermittedRequests(action, principal, resource).
```
