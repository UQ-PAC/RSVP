# RSVP Java CLI

[![RSVP Java CLI](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml)

## Prerequisite: Cedar Java

This project depends on [a fork of CedarJava](https://github.com/rebecca-odonoghue/cedar-java) which
can be built locally or pulled from the GitHub Packages repository. Both options are outlined below,
you need only complete one.

### Pull CedarJava from GitHub Packages:

Pulling artifacts from GitHub Packages repositories requires authentication with a personal access
token with read permissions. These permissions are granted automatically to CI builds, but you will
need to add a personal access token to your account in order to pull any of our deployed artifacts.

1. Create a personal access token with `read:packages` permission:
   [Account Settings > Developer settings > Personal access tokens > Tokens (classic) > Generate new token (classic) > check `read:packages` > Generate token]
2. Configure your environment such that Gradle has access to the following variables:
   ```
   PACKAGES_USER=<your_github_username>
   PACKAGES_TOKEN=<your_personal_access_token>
   ```

### Build and deploy CedarJava locally:

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

## Local development

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

## Import libraries in another project

If you want to use the libraries in this project in another project you can either deploy them
locally or pull them from GitHub Packages.

### Deploy locally

1. Deploy libraries to local repository:

```
./gradlew publishToMavenLocal
```

2. Import libraries by adding the following to `build.gradle`:

```
repositories {
    mavenLocal();
    // any other repositories
}

dependencies {
    implementation 'uq.pac.rsvp:policy-ast:1.0.1-SNAPSHOT'
    // any other dependencies
}

```

### Pull from GitHub Packages

Just as with CedarJava, a personal access token with `packages:read` permission is required to read
packages from GitHub Packages repositories.

1. Create a personal access token with `read:packages` permission:
   [Account Settings > Developer settings > Personal access tokens > Tokens (classic) > Generate new token (classic) > check `read:packages` > Generate token]

2. Import libraries by adding the following to `build.gradle`:

   ```
   repositories {
       maven {
           url = 'https://maven.pkg.github.com/rebecca-odonoghue/cedar-java'
           credentials {
               username = System.getenv('PACKAGES_USER')
               password = System.getenv('PACKAGES_TOKEN')
           }
       }
       maven {
           url = 'https://maven.pkg.github.com/UQ-PAC/RSVP'
           credentials {
               username = System.getenv('PACKAGES_USER')
               password = System.getenv('PACKAGES_TOKEN')
           }
       }
   }

   dependencies {
      implementation 'uq.pac.rsvp:policy-ast:1.0.1-SNAPSHOT'
      // any other dependencies
   }
   ```

3. Configure your environment such that Gradle has access to the following variables:
   ```
   PACKAGES_USER=<your_github_username>
   PACKAGES_TOKEN=<your_personal_access_token>
   ```
