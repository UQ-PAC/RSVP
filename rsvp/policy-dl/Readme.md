# Datalog (Soufflé) Encoding of Cedar Policies

This project describes a prototype that translates Cedar policies to Datalog aiming to generate
sets of all requests permitted/forbidden by the policies with respect to schema and provided set
of entities.

## Build

```
./gradlew build
```

## Prototype Functionality

The initial prototype tool is available via the `policy-dl-all.jar` shadow jar. 
For the moment the tool accepts the following arguments:
+ `--schema`: Cedar schema (Cedar)
+ `--policies`: Cedar policies (Cedar)
+ `--entities`: Cedar entities (Json)
+ `--datalog-dir`: A directory for Datalog outputs 

The tool translates Cedar components to Souffle Datalog specification
(see [translation details](Translation.md) for further information), 
executes it and loads the results back to Java. This computation generates
the list of all valid Cedar-level requests (as (`principal`, `action`, `resource`) triples)
that can either be permitted or forbidden by the supplied policy. 

The tool further has an ability to validate all generated requests using Cedar and report 
whether any discrepancies between Cedar and RSVP authorisation engines have been found.
This functionality can be enabled via the `--validate` option.


## Datalog output

Outputs at the Datalog level are written into TAB-separated CSV files, where each line 
captures `principal`, `resource` and `action`.

+ auth.dl - Datalog specification
+ Forbid\<PolicyName\>.csv - requests explicitly forbidden by a given permit policy
+ Permit\<PolicyName\>.csv - requests explicitly permitted by a given permit policy
+ Permit.csv - requests explicitly permitted all policies in the supplied policy set
+ Forbid.csv - requests explicitly forbidden all policies in the supplied policy set
+ PermittedRequests.csv - all requests permitted by the policy
+ ForbiddenRequests.csv - all requests forbidden by the policy
+ ActionableRequests.csv - all valid requests WRT to the supplied schema, policy and a list of entities

## Example

Example (assuming `RSVP/rsvp` as a current working directory):

```
java -jar policy-dl/build/libs/policy-dl-all.jar \
    --schema  src/test/resources/translation/petclinic/petclinic.cedarschema \
    --policies src/test/resources/translation/petclinic/petclinic.cedar \
    --entities src/test/resources/translation/petclinic/entities.cedar \
    --datalog-dir /tmp/rsvp-dl
```

