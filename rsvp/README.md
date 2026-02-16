# RSVP Java CLI

## Local development

Before building this project, you will need to build and deploy CedarJava locally from
[our fork of the CedarJava repository](https://github.com/rebecca-odonoghue/cedar-java).

- Build and deploy CedarJava:
  1. Clone CedarJava somewhere on your machine:
     ```
     git clone git@github.com:rebecca-odonoghue/cedar-java.git
     ```
  2. Build CedarJava:
     ```
     cd CedarJava
     ./gradlew build
     ```
  3. Optionally, run the Rust test suite that is not executed as part of the Gradle build:
     ```
     cd CedarJavaFFI
     cargo test
     ```
  4. Deploy CedarJava locally:
     ```
     cd CedarJava
     ./gradlew publishToMavenLocal
     ```

- Build:

  ```
  ./gradlew build
  ```

- Run tests and output coverage to `app/build/reports/jacoco/test/html/index.html`:

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

## Implement a new visitor

Visitor classes can be found in the `uq.pac.rsvp.ast.visitor` package. Visitors that don't need to visit every node but would still like to traverse the entire tree can extend the `PolicyVisitorImpl` class. Otherwise, interface `PolicyVisitor` can be implemented directly.

The JSON schema for the serialised Java AST defined in `app/src/main/resources/ast.schema.json`.

## Using the library in another project

In `build.gradle` in the project:

```
repositories {
    mavenLocal();
    // (any other repositories here)
}

dependencies {
    implementation 'uq.pac.rsvp.ast:rsvp-ast-lib:1.0.0'
}

```
