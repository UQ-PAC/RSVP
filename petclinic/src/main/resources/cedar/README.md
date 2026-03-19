# Cedar Authorization Files

This folder contains the Cedar files for both the original and RSVP's versions of PetClinic, including request files for testing.

## Original

Folder `./original`.

To run all tests: `./petclinic-original-requests.sh`.

All tests should return `ALLOW`.

Request JSON files are named following the convention: `<principal>-<action>-<resource>.json`.

## RSVP

Folder `./rsvp`.

To run all tests: `./petclinic-rsvp-requests.sh`.

To run all tests with verbose output: `./petclinic-rsvp-requests-verbose.sh`.

The commands above will load the default Cedar files:
- Schema: `petclinic-rsvp-schema.cedarschema`
- Entities: `petclinic-rsvp-entities.json`
- Policy: `petclinic-rsvp-policy.cedar`

You can change the default Cedar Policy by running, instead:
- `./petclinic-rsvp-requests.sh <another-policy.cedar>`
- `./petclinic-rsvp-requests-verbose.sh <another-policy.cedar>`

Request JSON files are named following the convention: `<principal>-<action>-<resource>.json`.

## Graphviz Visualisation of Entities

1. Install the [`xdot`](https://github.com/jrfonseca/xdot.py) package.
On Debian, run: `sudo apt install xdot`.

2. Update the respective Graphviz dot file.
Inside the respective folder, run, e.g.: `cedar visualize --entities petclinic-rsvp-entities.json > petclinic-rsvp-entities.dot`.

3. Run `xdot`, e.g.: `xdot petclinic-rsvp-entities.dot`.
