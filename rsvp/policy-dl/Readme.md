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
+ [Schema](examples/pohotoapp/schema.cedarschema)
+ [Policies](examples/pohotoapp/policy.cedar)
+ [Entities](examples/pohotoapp/entities.json)
+ [Souffle Datalog Encoding](examples/pohotoapp/auth.dl)

# Cedar Language Subset

## Cedar Policies

#bclose
    ## Principal Scope

```
principal
principal == Entity
principal is Type
principal in Entity          // Unsupported
principal is Type in Entity  // Unsupported
```

### Resource Scope

```
resource 
resource == Entity
resource is Type
resource in Entity          // Unsupported
resource is Type in Entity  // Unsupported
```

### Action Scope

```
action
action == Entity
action in Entity                 // Unsupported
action in [ Entity, ..., Entity] // Unsupported
```

### Conditions

TBD

## Cedar Schema

