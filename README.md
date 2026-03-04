# Resilient and Secure systems through Verified security Policies (RSVP)

## Policy generation

A Gradle project (in `./policygen`) for generating random Cedar policies for testing purposes.

## Java CLI and libraries for policy verification

[![RSVP Java CLI](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/rsvp.yaml)

Gradle projects in `./rsvp`:

- `app`: Java CLI
- `policy-ast`: AST library for modelling policies and schemas in Java
- `policy-dl`: Datalog encoding of Cedar policies

## Web application

Node/React application (in `./webapp`) provides a GUI for running queries using the Java CLI
and displaying the resulting reports in a user-friendly way.
