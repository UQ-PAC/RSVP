# Policy generator

Work-in-progress.

This program will generate randomized Cedar policies based on a schema. It goes to some effort to
ensure the individual policies "make sense" (in terms of types etc).

## Build requirements

The project requires that the RSVP "policy-ast" library has been built and deployed to the local
Maven artifact repository (found in: `../rsvp`).

## Instructions

This project uses the Gradle "application" plugin.

 - `./gradlew assemble` - build
 - `./gradlew run` - run
 
Generated policies will be output to "app/policy-out.cedar".
