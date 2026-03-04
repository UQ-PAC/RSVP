# Cedar Authorization Files

This folder contains the Cedar files for both the original and RSVP's versions of PetClinic, including request files for testing.

## Original

Folder `./original`.

To run all tests: `./petclinic-original-requests.sh`.

All tests should return `ALLOW`.

## RSVP

Folder `./rsvp`.

### Run Tests

To run all tests: `./petclinic-rsvp-requests.sh`.

## Graphviz Visualisation of Entities

1. Install the [`xdot`](https://github.com/jrfonseca/xdot.py) package.
On Debian, run: `sudo apt install xdot`.

2. Update the respective Graphviz dot file.
Inside the respective folder, run, e.g.: `cedar visualize --entities petclinic-rsvp-entities.json > petclinic-rsvp-entities.dot`.

3. Run `xdot`, e.g.: `xdot petclinic-rsvp-entities.dot`.
