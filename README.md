# Resilient and Secure systems through Verified security Policies (RSVP)

## Policy generation

A Gradle project (in `./policygen`) for generating random Cedar policies for testing purposes.

## Java applications and libraries for policy verification

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

## Web application

[![Build Web Application](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml)

React application (in `./webapp`) provides a GUI for running queries using the Java libraries
and displaying the resulting reports in a user-friendly way.
