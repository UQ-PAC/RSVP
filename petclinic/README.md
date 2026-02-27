# Spring PetClinic Sample Application

Spring PetClinic as of commit `e316074`.

Copy of the files from the original [spring-petclinic](https://github.com/spring-projects/spring-petclinic) repository as of commit [`e316074300de4761f7b100a21423525e8b8ecdc5`](https://github.com/spring-projects/spring-petclinic/commit/e316074300de4761f7b100a21423525e8b8ecdc5).

Includes only the necessary files to run the application locally using Gradle, i.e. excludes files for Maven, Docker, Kubernetes, and GitHub Workflows.

## Run Petclinic locally

Spring Petclinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using Gradle.
Java 21 or later is required for the build, and the application can run with Java 21 or newer.

You can start the application on the command-line as follows:

```bash
./gradlew bootRun --no-daemon
```

You can then access the Petclinic at <http://localhost:8080/>.

## Database configuration

In its default configuration, Petclinic uses an in-memory database (H2) which gets populated at startup with data.
The h2 console is exposed at `http://localhost:8080/h2-console`, and it is possible to inspect the content of the database using the `jdbc:h2:mem:<uuid>` URL.
The UUID is printed at startup to the console.

A similar setup is provided for MySQL and PostgreSQL if a persistent database configuration is needed.
Note that whenever the database type changes, the app needs to run with a different profile: `spring.profiles.active=mysql` for MySQL or `spring.profiles.active=postgres` for PostgreSQL.
See the [Spring Boot documentation](https://docs.spring.io/spring-boot/how-to/properties-and-configuration.html#howto.properties-and-configuration.set-active-spring-profiles) for more detail on how to set the active profile.

You can start MySQL or PostgreSQL locally with whatever installer works for your OS or use docker:

```bash
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:9.5
```

or

```bash
docker run -e POSTGRES_USER=petclinic -e POSTGRES_PASSWORD=petclinic -e POSTGRES_DB=petclinic -p 5432:5432 postgres:18.1
```

Further documentation is provided for [MySQL](src/main/resources/db/mysql/petclinic_db_setup_mysql.txt) and [PostgreSQL](src/main/resources/db/postgres/petclinic_db_setup_postgres.txt).

## Test Applications

At development time we recommend you use the test applications set up as `main()` methods in `PetClinicIntegrationTests` (using the default H2 database and also adding Spring Boot Devtools), `MySqlTestApplication` and `PostgresIntegrationTests`.
These are set up so that you can run the apps in your IDE to get fast feedback and also run the same classes as integration tests against the respective database.
The MySql integration tests use Testcontainers to start the database in a Docker container, and the Postgres tests use Docker Compose to do the same thing.

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

