# RSVP Policy AST

## Implement a new policy visitor

Visitor classes can be found in the `uq.pac.rsvp.policy.ast.visitor` package.

There are two types of visitors, visitors that return `void` from their visit methods, and visitors that return a
generic typed value. Visitors that return void should implement `PolicyVisitor` or `SchemaVisitor`. If they don't
need to visit every node type but would still like to traverse the entire tree, `PolicyVisitorImpl` or
`SchemaVisitorImpl` can be extended instead.

Visitors that return a value from their `visit` methods should implement `PolicyComputationVisitor` or
`SchemaComputationVisitor` and call the generic `compute` method on each `AST` node, rather than `accept`.

The JSON schema for the serialised Java AST is defined in `lib/src/main/resources/ast.schema.json`. A
schema for the Cedar JSON schema syntax can be found in the [Cedar VSCode Extension repository](https://raw.githubusercontent.com/cedar-policy/vscode-cedar/refs/heads/main/schemas/cedarschema.schema.json).

## Programmatically construct schema AST

As schemas contain type references, there is a type resolution pass that is executed after
parsing. If you are constructing a schema manually, some constructs (particularly recursive type definitions)
might not be possible to construct using resolved type reference classes.

In this case, and probably in any case, the easiest way to create type references is to use instances of
`UnresolvedTypeReference` and then run the type resolution pass once your schema is complete.

```
// ...

record.put("attribute", new UnresolvedTypeReference("Namespace::Attribute"));

//...

schema.accept(new SchemaResolutionVisitor());

//...
```

See `uq.pac.rsvp.policy.ast.schema.SchemaTest.TestManual` for examples.
