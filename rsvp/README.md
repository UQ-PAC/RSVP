# RSVP Java CLI

[![Build Java CLI](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml)

## Environment setup

### GitHub Packages

This project pulls dependencies from GitHub Packages repositories, which require authentication
for read access. Gradle expects to find the required authentication credentials in the system
properties `packages.user` and `packages.token`.

You will need to create these credentials in GitHub and supply them to Gradle as system properties.

1. Create a personal access token with `read:packages` permission:

   Account Settings > Developer settings > Personal access tokens > Tokens (classic) > Generate new token (classic) >
   check `read:packages` > Generate token

2. Add the system properties to your user-scoped properties file (`~/.gradle/gradle.properties`):
   ```
   systemProp.packages.user=<your_github_username>
   systemProp.packages.token=<your_personal_access_token>
   ```

### Soufflé

Soufflé is required to build and run the `policy-dl` project, and can be installed using a package manager:

- Ubuntu (via APT)
  ```
  wget https://github.com/souffle-lang/souffle/releases/download/2.5/x86_64-ubuntu-2404-souffle-2.5-Linux.deb
  sudo apt install ./x86_64-ubuntu-2404-souffle-2.5-Linux.deb
  ```
- macOS (via Homebrew)
  ```
  brew install souffle
  ```
- Other downloads from [their GitHub releases page](https://github.com/souffle-lang/souffle/releases)

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
  ./gradlew :app:run --args="optional string of space separated args"
  ```

  Arguments include:

    - Specify a schema file: `--schema <file>`/`-s <file>`
    - Specify one or more policies files: `--policies <files>`/`-p <files>`

      Files specified after a single `--policies` or `-p` argument will be treated as versions of the same file in the
      order they are specified. To specify multiple policy files that aren't versions of the same file, supply them
      separately (with `--policies` or `-p` before each file).
    - Specify an entities JSON file: `--entities <file>`/`-e <file>`
    - Specify an invariants file (optional): `--invariants <file>`/`-i <file>`


- Run web server:

  ```
  ./gradlew :webserver:bootRun
  ```

## Import libraries in another project

If you want to use the libraries in this project in another project you can either deploy them
locally or pull them from GitHub Packages.

### Deploy libraries locally

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
       // Note: check for latest released version, version numbers here are examples
       implementation 'uq.pac.rsvp:policy-ast:1.1.2-SNAPSHOT'
       implementation 'uq.pac.rsvp:support:1.0.0-SNAPSHOT'
       implementation 'uq.pac.rsvp:verification:1.0.0-SNAPSHOT'
       // any other dependencies
   }
   ```

### Pull libraries from GitHub Packages

1. Follow the [instructions above](#github-packages) to set up your environment to pull from GitHub Packages.

2. Import libraries by adding the following to `build.gradle`:

   ```
   repositories {
       maven {
           url = 'https://maven.pkg.github.com/UQ-PAC/RSVP'
           credentials {
               username = System.getProperty('packages.user')
               password = System.getProperty('packages.token')
           }
       }
   }

   dependencies {
       // Note: check for latest released version, version numbers here are examples
       implementation 'uq.pac.rsvp:policy-ast:1.1.2-SNAPSHOT'
       implementation 'uq.pac.rsvp:support:1.0.0-SNAPSHOT'
       implementation 'uq.pac.rsvp:verification:1.0.0-SNAPSHOT'
       // any other dependencies
   }
   ```
