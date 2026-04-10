# Cedar Authorization Files

This folder contains the Cedar files for the RSVP version of ChildClinic, ChildClinic, including request files for testing.

To run all tests: `./childclinic-rsvp-requests.sh`.

The commands above will load the default Cedar files:
- Schema: `childclinic-rsvp-schema.cedarschema`
- Entities: `childclinic-rsvp-entities.json`
- Policy: `childclinic-rsvp-policy.cedar`

You can change the default Cedar Policy by running, instead:
- `./childclinic-rsvp-requests.sh -p <another-policy.cedar>`

Available flags:
- `-t`: to read requests from the `./ALLOW-TEST` and `./DENY-TEST` folders instead of from the `./ALLOW` and `./DENY` folders.
- `-v`: to get verbose output, which includes the actual resquest.
- `-p`: to load another Cedar policy instead.

Request JSON files in the `./ALLOW-TEST` and `./DENY-TEST` folders are named following the convention: `<principal>-<action>-<resource>.json`.

## Graphviz Visualisation of Entities

1. Install the [`xdot`](https://github.com/jrfonseca/xdot.py) package.
On Debian, run: `sudo apt install xdot`.

2. Update the Graphviz dot file.
Run: `cedar visualize --entities childclinic-rsvp-entities.json > childclinic-rsvp-entities.dot`.

3. Run `xdot`, e.g.: `xdot childclinic-rsvp-entities.dot`.

## Authorisation Model for the RSVP ChildClinic App

Cedar [Schema](childclinic-rsvp-schema.cedarschema).

### Entities

All _actors_ are either an entity `Employee` or an entity `Guest`.

Every `Employee` must have a `name`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Role` in their set `roles`.
`Role` is either `Administrator`, `Secretary`, or `Doctor`.

Optional `Employee` attributes are `level`, where entity `Level` is either `Intern`, `Resident`, `Staff`, `Senior`, `Registrar`, or `Specialist`, and `manager`, which is an entity `Employee`.
For `Level`, the values `Resident`, `Registrar`, and `Specialist` are only used in conjunction with `Role` `Doctor`.

`Guest` has no attributes.

Non-_actors_ are entities `Parent`, `Child`, and `Visit`.

Evey `Parent` must have a `name`, at least one entity `Child` in their set `children`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Doctor` in their set `doctors`.

Evey `Child` must have a `name`, one entity `Parent` as their `parent`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Doctor` in their set `doctors`.

Every `Visit` must have one entity `Child` as their `child`, at least one entity `Clinic` in their set `clinics`, at least one entity `Doctor` in their set `doctors`, and one entity `Confidentiality` as their `confidentiality`.
`Confidentiality` is either `Official`, `Sensitive`, or `Protected`.

### Actions

Actions are separated into two groups, `ClientOperations` and `EmployeeOperations`, and every action applies to a principal whose entity is either `Employee` or `Guest`.

Actions `ViewClient`, `EditClient`, and `DeleteClient` apply to a resource whose entity is either `Parent`, `Child`, or `Visit`.
Actions `ViewEmployee`, `EditEmployee`, and `DeleteEmployee` apply to a resource whose entity is `Employee`.
Actions `AddClient` and `AddEmployee` apply to a resource whose entity is `Clinic`.
