# Cedar Authorization Files

This folder contains the Cedar files for both the original and RSVP's versions of PetClinic, including request files for testing.

- `./original`: All tests should return `ALLOW`.
- `./rsvp`

## Run Tests

To run all tests: `./petclinic-original-requests.sh`.

### Run Cedar Schema Validation

`cedar validate -s petclinic-original-schema.cedarschema -p petclinic-original-policy.cedar`

### Run Cedar Authorization Tests

- `cedar authorize --policies petclinic-original-policy.cedar --schema petclinic-original-schema.cedarschema --entities petclinic-original-entities.json --request-json ALLOW/visitor-findowner-findowners.json`
- `cedar authorize --policies petclinic-original-policy.cedar --schema petclinic-original-schema.cedarschema --entities petclinic-original-entities.json --request-json ALLOW/visitor-findowner-home.json`
- `cedar authorize --policies petclinic-original-policy.cedar --schema petclinic-original-schema.cedarschema --entities petclinic-original-entities.json --request-json ALLOW/visitor-findowner-owner.json`
- `cedar authorize --policies petclinic-original-policy.cedar --schema petclinic-original-schema.cedarschema --entities petclinic-original-entities.json --request-json ALLOW/visitor-viewwebpage-home.json`
