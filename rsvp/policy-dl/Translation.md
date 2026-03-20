# Cedar to Datalog Translation

## Entities and actions

Each cedar entity type is tracked via a unary relation, and the facts of that relation are 
Cedar Entity Identifiers (EUID). Such entity relations are named `Entity_<T>`, where `N`
is the name of the entity type. Actions are treated similarly all action UIDs belong to the 
unary `Action` relation. For instance, the following cedar fragment

```
entity Role enum [ "User", "Admin" ];
entity Account = { 
    age: Long,
    role: Role
};

action view appliesTo {
    principal: Account,
    resource: Role
} 
```
translates to

```
.decl Entity_Account(id: symbol)
Entity_Account("Account::Alice").
Entity_Account("Account::Bob").
Entity_Account("Account::???").

.decl Entity_Role(id: symbol)
Entity_Role("Role::User").
Entity_Role("Role::Admin").

.decl Action(id: symbol)
Action("Action::view").
```

assuming there exist entities `Account::"Bob"` and `Account::"Alice"`.
Special EUID `"???"` is added for any non-enum entity definition and
denotes an _unknown_ (or open-world) entity that does not belong to the
input entity set.
Since cedar assumes that all actions are _fixed_, such _unknown_ 
actions are omitted from the action relation.

## Entity attributes

Entity attributes are tracked via relations

+ `.decl HasAttribute(euid: symbol, attr: symbol)`, and 
+ `.decl Attribute(euid: symbol, attr: symbol, value: symbol)`

### Attribute relation

The `Attribute` relation associates EUIDs with attribute names and corresponding values.
For instance, the following entity 

```
    {
        "uid": { "type": "File", "id": "ls" },
        "attrs": {
            "size": 256,
            "kind": "executable",
            "owner" : User,
            "permissions": [ "r", "w", "x" ]
        }
        "parents": [ ]
    }
```

is translated to

```
Attribute("File::ls", "owner", "User::"Alice" )
Attribute("File::ls", "size", "256")
Attribute("File::ls", "kind", "executable")
Attribute("File::ls", "prtmissions", "r")
Attribute("File::ls", "prtmissions", "w")
Attribute("File::ls", "prtmissions", "x")
```

<em>NOTE: regardless of type, at the Datalog level all data is kept as strings and then
converted via Souffle functors as needed.</em>

Since Datalog facts have linear structure, to track record structure we create temporary
EUIDs on and use them as references. For example, consider the following cedar entity type 
```
entity Fiie = {
    attrs: {
        permissions: {
            read: Bool,
            write: Bool
        }
    }
}
```
and its instance
```
{
    "uid": { "type": "File", "id": "ls" },
    "attrs": {
        "permissions": {
            "read": true,
            "write": false
        },
    }
    "parents": [ ]
}
```

To capture these attributes we first generate a unique EUID `Tmp::EUID::File::"ls"`
that maps to `read` and `write` attributes and then map EUID `File::"ls"` to attribute
`permissions` as follows:

```
Attribute("File::ls", "permissions", "Tmp::EUID::File::"ls")
Attribute("Tmp::EUID::File::"ls", "read", "true")
Attribute("Tmp::EUID::File::"ls", "write", "false")
```

### HasAttribute relation

`HasAttribute` maps EUIDs to names of attributes they have (including temporary 
EUIDs created for `Attribute`). The above cedar example generates the following
`HasAttribute` facts.

```
HasAttribute("File::ls", "permissions")
HasAttribute("Tmp::EUID::File::"ls", "read")
HasAttribute("Tmp::EUID::File::"ls", "write")
```

`HasAttribute` allows to identify whether an entity has a given attribute or not.
This cannot be always achieved through the `Attribute` relation alone because an attribute
can be mapped to an empty set have not associated `Attribute` relation facts.

## Actions

Cedar actions provide limits for their application with respect to principals and resources 
they can apply to, e.g., in the following example action `Action::"view"` can only apply to 
principals of type `Account` and resources of type `Role`.

```
action "view" appliesTo {
    principal: [Account],
    resource: [Role]
} 
```

To track these dependencies the following input relations are used.

+ `.decl ActionPrincipal(action: symbol, principal: symbol)`, and
+ `.decl ActionResource(action: symbol, resource: symbol)`

Here, `ActionPrincipal` (resp. `ActionResource`) associates actions with principals (resp. resources)
these actions apply to. The above action generates the following rules:

```
ActionPrincipal("Action::view", principal) :- Entity_Account(principal).
ActionResource("Action::view", resource) :- Entity_Role(resource).
```

## Actionable requests

`ActionPrincipal` and `ActionResource` actions can then be used to construct the set of
`(principal, resource, action)` triples that form the set of all valid (or actionable requests)
for which a definitive permit ot forbid decision can be made. These actionable requests
are captured via the `ActionableRequests` rule defined as follows:

```
.decl ActionableRequests(principal: symbol, resource: symbol, action: symbol)
ActionableRequests(principal, resource, action) :-
    ActionPrincipal(action, principal),
    ActionResource(action, resource).
```

## Parent hierarchies

To track parent information of entities and actions this translation uses a (transitive and 
reflexive) binary relation called `ParentOf` that maps EUIDs or ancestors to the EUIDs of 
descendants. Consider the following Cedar fragment

```
entity Partition;
entity Directory in Partition;
entity File in Directory;

action manage;
action view in [manage] appliesTo {
    principal: Account,
    resource: Role
} 
```
Assuming input entities `Partition::"SDA"`, `Directory::"bin"` and `File::"ls"` 
this fragment translates to.

```
.decl ParentOf(parent: symbol, child: symbol)
ParentOf("Partition::SDA", "Directory::bin").
ParentOf("Directory::bin", "File::ls").
ParentOf("Action::manage", "Action::view").

// Reflexivity
ParentOf("Partition::SDA", "Partition::SDA").
ParentOf("Directory::bin", "Directory::bin").
ParentOf("Action::view", "Action::view").
ParentOf("Action::manage", "Action::manage").

// Transitivity
ParentOf(x, z) :- ParentOf(x, y), ParentOf(y, z).
```

## Policy Set

Each Cedar policy is translated to a ternary Datalog rule over variables 
`principal`, `resource` and `action`. The generated tuples of such relation
are requests that are either explicitly permitted or forbidden by the policy
(depending on whether a policy is `permit` or `forbid`). Policy translation is done in stages.

Firstly, a policy is compacted into a single boolean-valued Cedar expression by creating 
a disjunction over `principal`, `resource`, `action`, `when` and negated `unless` clauses.
The policy formula is then converted to disjunctive normal form. Each disjunction of the formula
becomes a rule, and each conjunction becomes a rule expression (an atom or a constraint).

Consider policy (see [photoapp.cedarschema](src/test/resources/translation/photoapp/photoapp.cedarschema)
for schema details)
```
permit (
    principal is Account,
    action == Action::"viewPhoto",
    resource is Photo)
when {
    resource.album.visibility == Visibility::"Public"
};
```
The policy is compacted into a formula and converted to DNF
```
principal is Account &&
    action == Action::"viewPhoto" &&
    resource is Photo &&
    resource.album.visibility == Visibility::"Public" 
```
That generates the following Datalog rule

```
// Permit Policy: PermitPolicy1
.decl PermitPolicy1(principal: symbol, resource: symbol, action: symbol)
PermitPolicy1(principal, resource, action) :-
    // Ground terms
    ActionPrincipal(action, principal),
    ActionResource(action, resource),
    // (principal is Account),
    Entity_Account(principal),
    // (action == Action::"viewPhoto"),
    action = "Action::\"viewPhoto\"",
    // (resource is Photo),
    Entity_Photo(resource),
    // (resource.album.visibility == Visibility::"Public"),
    Attribute(resource, "album", var0),
    Attribute(var0, "visibility", var1),
    var1 = "Visibility::Public".    
```

that captures all requests explicitly allowed by this policy.

Translation of individual Cedar expressions into Datalog rule expressions is discussed further.

The results of all permit and forbid policies from an input policy set are combined into relations
`Permit` and `Forbid` that specify all requests explicitly permitted or forbidden
the given combination of policies. For the example above with only one permit policy 
these relations are as follows:

```
// General Permit Rule (requests explicitly allowed by the policy)
.decl Permit(principal: symbol, resource: symbol, action: symbol)
Permit(principal, resource, action) :-
    PermitPolicy1(principal, resource, action).

// General Forbid Rule (requests explicitly forbidden by the policy)
.decl Forbid(principal: symbol, resource: symbol, action: symbol)
```

Finally, the complete set of requests permitted and forbidden by the input policy set
are generated via the following output relations:

```
// All permitted requests
.decl PermittedRequests(principal: symbol, resource: symbol, action: symbol)
PermittedRequests(principal, resource, action) :-
    Permit(principal, resource, action),
    !Forbid(principal, resource, action).

// All forbidden requests
.decl ForbiddenRequests(principal: symbol, resource: symbol, action: symbol)
ForbiddenRequests(principal, resource, action) :-
    ActionableRequests(principal, resource, action),
    !PermittedRequests(principal, resource, action).
```

## Cedar Expressions 

### Property access and comparison operators

Property access expressions, such as `resource.album.visibility` in the policy example above
is translated via `Attribute` relation over elements of the expression. This example translates
to 
```
Attribute(resource, "album", var0),
Attribute(var0, "visibility", var1)
```

Comparison operators are translated to Souffle constraints over generated variables, e.g.,
`resource.album.visibility == Visibility::"Public"` becomes `var1 == "Visibility::Public"`
at the Datalog level. 

For arithmetic comparison we use the `to_number` built-in Souffle functor, e.g.,
`principal.age > 26` becomes 
```
Attribute(principal, "age", var0),
to_number(var0) > 26
```

### Has operator

Cedar `has` operator is implemented via the `HasAttribute` input relation, e.g.,
`principal has "album"` becomes `HasAttribute(principal, "album")`

### Is operator

Cedar `is` operator maps directly to entity relations, e.g.,
`principal is Account` translates to `Entity_Account(principal)`

### In operator

Cedar `in` hierarchy membership operator is implemented using `ParentOf` relation.
`principal in Account::"Alice"` translates to `ParentOf("Account::Alice", principal)`

### Boolean literals

`true` does nto translate to anything because we start with a complete set of actionable requests
and then reduce it. `false` (that basically selects nothing) is translated to 

```
NullifiedRequests(principal, resource, action)
```
with `NullifiedRequests` being an empty relation.


### Set .contains

Expression of the form `principal.friends.contains(resource)` translates to

```
HasAttribute(principal, "friends"),
count : { Attribute(principal, "friends", resource) } >= 1
```

with the help of souffle `count` aggregate. 
Note that we need an additional `HasAttribute` atom to avoid _unknown_ relations to be considered.

### Set .isEmpty

Expression of the form `principal.friends.contains(resource)` translates to

```
Attribute(principal, "permissions", var0),
HasAttribute(var0, "roles"),
count : { Attribute(var0, "roles", _) } = 0
```

### Set .containsAny
Expression `pincipal.friends.contains(resource.friends)` translates to 

```
HasAttribute(principal, "traits"),
HasAttribute(resource, "traits"),
count : { Attribute(principal, "traits", var0), Attribute(resource, "traits", var0) } >= 1,
```

### Set .containsAll
Expression `principal.friends.contains(resource.friends)` translates to

```
HasAttribute(principal, "traits"),
HasAttribute(resource, "traits"),
count : { Attribute(principal, "traits", var0), Attribute(resource, "traits", var0) } = count : { Attribute(resource, "traits", _) },
```
