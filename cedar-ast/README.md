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

## Implement a new visitor

Visitor classes can be found in the `uq.pac.rsvp.ast.visitor` package. Visitors that don't need to visit every node but would still like to traverse the entire tree can extend the `PolicyVisitorImpl` class. Otherwise, interface `PolicyVisitor` can be implemented directly.

The JSON schema for the serialised Java AST defined in `app/src/main/resources/ast.schema.json`.
