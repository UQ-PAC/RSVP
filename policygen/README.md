# Policy generator

Work-in-progress.

This program will generate randomized but somewhat feasible Cedar policies based on a schema. It
goes to some effort to ensure the individual policies "make sense" (in terms of types etc).


## Instructions

This project uses the Gradle "application" plugin.

 - `./gradlew assemble` - build
 - `./gradlew run` - run
 - `./gradlew run --args="space-separated-args"` - run with arguments
 
 Arguments accepted:
 
 - `--schema <schema-file.cedarschema>` - generate based on given schema
 - `--entities <entities.json>` - generated based on the given entities
 
When run via Gradle, generated policies will be output to "app/policy-out.cedar".
