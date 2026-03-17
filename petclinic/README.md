# Spring PetClinic Sample Application

Based on Spring PetClinic as of commit `e316074`.

Initial files copied from the original [spring-petclinic](https://github.com/spring-projects/spring-petclinic) repository as of commit [`e316074300de4761f7b100a21423525e8b8ecdc5`](https://github.com/spring-projects/spring-petclinic/commit/e316074300de4761f7b100a21423525e8b8ecdc5).

Includes only the necessary files to run the application locally using Gradle and H2 database, i.e. excludes files for Maven, Docker, Kubernetes, MySQL, PostgreSQL, and GitHub Workflows.

## Run Petclinic locally

Spring Petclinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using Gradle.
Java 21 or later is required for the build, and the application can run with Java 21 or newer.

You can start the application on the command-line as follows:

```bash
./gradlew bootRun --no-daemon
```

You can then access the Petclinic at <http://localhost:8080/>.

The command above will load the default Cedar files:
- Schema: `src/main/resources/cedar/rsvp/petclinic-rsvp-schema.cedarschema`
- Entities: `src/main/resources/cedar/rsvp/petclinic-rsvp-entities.json`
- Policy: `src/main/resources/cedar/rsvp/petclinic-rsvp-policy.cedar`

You can change the default Cedar Policy by running, instead:

```bash
./gradlew bootRun --no-daemon --args="--policy.file=<another-policy.cedar>"
```

## Database configuration

In its default configuration, Petclinic uses an in-memory database (H2) which gets populated at startup with data.
The h2 console is exposed at `http://localhost:8080/h2-console`, and it is possible to inspect the content of the database using the `jdbc:h2:mem:<uuid>` URL.
The UUID is printed at startup to the console.

## Test Applications

At development time we recommend you use the test applications set up as `main()` methods in `PetClinicIntegrationTests` (using the default H2 database and also adding Spring Boot Devtools).
These are set up so that you can run the apps in your IDE to get fast feedback and also run the same classes as integration tests against the respective database.

## Compiling the CSS

There is a `petclinic.css` in `src/main/resources/static/resources/css`.
It was generated from the `petclinic.scss` source, combined with the [Bootstrap](https://getbootstrap.com/) library.
If you make changes to the `scss`, or upgrade Bootstrap, you will need to re-compile the CSS resources using the Maven profile "css", i.e. `./mvnw package -P css`.
There is no build profile for Gradle to compile the CSS.

## Looking for something in particular?

|Spring Boot Configuration | Class or Java property files  |
|--------------------------|---|
|The Main Class | [PetClinicApplication](src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java) |
|Properties Files | [application.properties](src/main/resources) |
|Caching | [CacheConfiguration](src/main/java/org/springframework/samples/petclinic/system/CacheConfiguration.java) |

## License

The Spring PetClinic sample application is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
