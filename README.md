# Resilient and Secure systems through Verified security Policies (RSVP)

## Subprojects

### Policy generation

A Gradle project (in `./policygen`) for generating random Cedar policies for testing purposes.

### Java applications and libraries for policy verification

[![Build Java CLI](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml)

Gradle projects in `./rsvp`:

- `app`: Java CLI (currently just placeholder, not useful)
- `webserver`: Spring Boot web server for interacting with the React web interface
  (runs in conjunction with the React application in `./webapp`)
- `policy-ast`: AST library for modelling policies and schemas in Java
- `policy-dl`: Datalog encoding of Cedar policies
- `support`: library with common interfaces/classes between other components
- `verification`: library to perform verification checks and generate reports (to be consumed by
  the web application, for example)

### Web application

[![Build Web Application](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml)

React application (in `./webapp`) provides a GUI for running queries using the Java libraries
and displaying the resulting reports in a user-friendly way.

### Cedar demonstration

A Gradle project (in `./childrenclinic`) for a Spring Boot application based on [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) to demonstrate Cedar.

## Build tooling and run verification locally

The prototype consists of several Java libraries, two Java command line applications and a web
application. These instructions are for running verification via the web application.

> [!NOTE]
> Although the tooling is cross-platform, it has been tested on only Linux and macOS. There
> is no guarantee that it will run correctly on Windows.
> The instructions below assume a `bash` or `zsh` terminal running in the root directory of the
> repository.

### Build and run Java libraries and web application back-end

1. Ensure you have Java Development Kit 21 or later installed

   ```
   java -version
   ```

   Install [from Oracle](https://www.oracle.com/java/technologies/downloads/#java21) or elsewhere, if required.

2. Install Soufflé
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

3. Build the Java libraries and start the web server back-end

   ```
   ./rsvp/gradlew -p ./rsvp :webserver:bootRun
   ```

4. For further instructions on the Java tooling, see [rsvp/README.md](https://github.com/UQ-PAC/RSVP/blob/main/rsvp/README.md).

### Build and run Node.js and React.js web application front end

> [!NOTE]
> The application has been tested using only Firefox. Some additional issues have been detected in
> Google Chrome, so it is suggested that Firefox be used to access the web application.

1. Ensure you have Node.js 24 installed

   ```
   node -v
   ```

   Install Node.js by following the relevant instructions in the [npm documentation](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm), if required.

2. Install pnpm using npm

   ```
   npm install -g pnpm@latest-10
   ```

3. Install project dependencies using pnpm

   ```
   pnpm --dir ./webapp install
   ```

4. Build the application

   ```
   pnpm --dir ./webapp build
   ```

5. Run the front-end web server

   ```
   pnpm --dir ./webapp start
   ```

6. Access the web application by navigating to [http://localhost:3000](http://localhost:3000) in Firefox.

7. For further instructions on the web application front-end, see [webapp/README.md](https://github.com/UQ-PAC/RSVP/blob/main/webapp/README.md).

## Cedar policy examples

Examples of Cedar policies as well as RSVP-style invariants useable with the RSVP toolkit can be found in [examples](examples) directory.
