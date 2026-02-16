# RSVP Java CLI

## Local development

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
