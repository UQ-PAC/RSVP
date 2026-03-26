# Cedar Authorization Files

This folder contains the Cedar files for the RSVP version of PetClinic, including request files for testing.

To run all tests: `./petclinic-rsvp-requests.sh`.

The commands above will load the default Cedar files:
- Schema: `petclinic-rsvp-schema.cedarschema`
- Entities: `petclinic-rsvp-entities.json`
- Policy: `petclinic-rsvp-policy.cedar`

You can change the default Cedar Policy by running, instead:
- `./petclinic-rsvp-requests.sh -p <another-policy.cedar>`

Available flags:
- `-t`: to read requests from the `./ALLOW-TEST` and `./DENY-TEST` folders instead of from the `./ALLOW` and `./DENY` folders.
- `-v`: to get verbose output, which includes the actual resquest.
- `-p`: to load another Cedar policy instead.

Request JSON files in the `./ALLOW-TEST` and `./DENY-TEST` folders are named following the convention: `<principal>-<action>-<resource>.json`.

## Graphviz Visualisation of Entities

1. Install the [`xdot`](https://github.com/jrfonseca/xdot.py) package.
On Debian, run: `sudo apt install xdot`.

2. Update the Graphviz dot file.
Run: `cedar visualize --entities petclinic-rsvp-entities.json > petclinic-rsvp-entities.dot`.

3. Run `xdot`, e.g.: `xdot petclinic-rsvp-entities.dot`.

## Authorisation Model for the RSVP PetClinic App

Cedar [Schema](petclinic-rsvp-schema.cedarschema).

### Entities

All _actors_ are either an entity `Employee` or an entity `Guest`.

Every `Employee` must have a `name`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Role` in their set `roles`.
`Role` is either `Administrator`, `Secretary`, or `Veterinarian`.

Optional `Employee` attributes are `level`, where entity `Level` is either `Intern`, `Resident`, `Staff`, `Senior`, `Registrar`, or `Specialist`, and `manager`, which is an entity `Employee`.
For `Level`, the values `Resident`, `Registrar`, and `Specialist` are only used in conjunction with `Role` `Veterinarian`.

`Guest` has no attributes.

Non-_actors_ are entities `Owner`, `Pet`, and `Visit`.

Evey `Owner` must have a `name`, at least one entity `Pet` in their set `pets`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Veterinarian` in their set `veterinarians`.

Evey `Pet` must have a `name`, one entity `Owner` as their `owner`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Veterinarian` in their set `veterinarians`.

Every `Visit` must have one entity `Pet` as their `pet`, at least one entity `Clinic` in their set `clinics`, at least one entity `Veterinarian` in their set `veterinarians`, and one entity `Confidentiality` as their `confidentiality`.
`Confidentiality` is either `Official`, `Sensitive`, or `Protected`.

### Actions

Actions are separated into two groups, `ClientOperations` and `EmployeeOperations`, and every action applies to a principal whose entity is either `Employee` or `Guest`.

Actions `ViewClient`, `EditClient`, and `DeleteClient` apply to a resource whose entity is either `Owner`, `Pet`, or `Visit`.
Actions `ViewEmployee`, `EditEmployee`, and `DeleteEmployee` apply to a resource whose entity is `Employee`.
Actions `AddClient` and `AddEmployee` apply to a resource whose entity is `Clinic`.
