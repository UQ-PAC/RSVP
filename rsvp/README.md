# RSVP Java CLI

## Local development

Before building this project, you will need to build and deploy CedarJava locally from
[our fork of the CedarJava repository](https://github.com/rebecca-odonoghue/cedar-java).

- Build and deploy CedarJava:
  1. Clone CedarJava somewhere on your machine:
     ```
     git clone git@github.com:rebecca-odonoghue/cedar-java.git
     cd cedar-java
     ```
  2. Build CedarJava:
     ```
     ./CedarJava/gradlew build -p ./CedarJava
     ```
  3. Optionally, run the Rust test suite that is not executed as part of the Gradle build:
     ```
     cargo test --manifest-path CedarJavaFFI/Cargo.toml
     ```
  4. Deploy CedarJava locally:
     ```
     ./CedarJava/gradlew publishToMavenLocal -p ./CedarJava
     ```

- Build:

  ```
  ./gradlew build
  ```

- Run tests and generate reports:

  ```
  ./gradlew test
  ```

  Test report: `build/reports/tests/test/aggregated-results/index.html`
  Coverage report: `build/reports/jacoco/testSuiteCodeCoverageReport/html/index.html`

- Run CLI application:

  ```
  ./gradlew run --args="optional string of space separated args"
  ```

- Deploy libraries to local repository for use in other projects:

  ```
  ./gradlew publishToMavenLocal
  ```

  Then import libraries by adding to `build.gradle`:

  ```
  repositories {
      mavenLocal();
      // any other repositories
  }

  dependencies {
      implementation 'uq.pac.rsvp:policy-ast:1.0.0'
      // any other dependencies
  }

  ```

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

new SchemaResolutionVisitor().visitSchema(schema);

//...
```

See `uq.pac.rsvp.policy.ast.schema.SchemaTest.TestManual` for examples.
