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

- Run tests and output coverage to `**/build/reports/jacoco/test/html/index.html`:

  ```
  ./gradlew test
  ```

- Run CLI application:

  ```
  ./gradlew run --args="optional string of space separated args"
  ```

- Deploy library to local repository for use in other projects:
  ```
  ./gradlew publishToMavenLocal
  ```

## Implement a new policy visitor

Visitor classes can be found in the `uq.pac.rsvp.policy.ast.visitor` package.

There are two types of visitors, visitors that return `void` from their visit methods, and visitors that return a
generic typed value. Visitors that return void should implement `PolicyVisitor` or `SchemaVisitor`. If they don't
need to visit every node type but would still like to traverse the entire tree, `PolicyVisitorImpl` or
`SchemaVisitorImpl` can be extended instead.

Visitors that return a value from their `visit` methods should implement `PolicyComputationVisitor` or
`SchemaComputationVisitor` and call the generic `compute` method on each `AST` node, rather than `visit`.

The JSON schema for the serialised Java AST is defined in `lib/src/main/resources/ast.schema.json`. A
schema for the Cedar JSON schema syntax can be found in the [Cedar VSCode Extension repository](https://raw.githubusercontent.com/cedar-policy/vscode-cedar/refs/heads/main/schemas/cedarschema.schema.json).

## Using the policy AST library in another project

Include in `build.gradle`:

```
repositories {
    mavenLocal();
    // (any other repositories here)
}

dependencies {
    implementation 'uq.pac.rsvp:policy-ast:1.0.0'
}

```
