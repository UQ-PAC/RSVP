# Cedar Authorization Files

This folder contains the Cedar files for the RSVP version of PetClinic, ChildrenClinic, including request files for testing.

To run all tests: `./childrenclinic-requests.sh`.

The commands above will load the default Cedar files:
- Schema: `childrenclinic.cedarschema`
- Entities: `childrenclinic-entities.json`
- Policy: `childrenclinic.cedar`

You can change the default Cedar Policy by running, instead:
- `./childrenclinic-requests.sh -p <another-policy.cedar>`

Available flags:
- `-t`: to read requests from the `./ALLOW-TEST` and `./DENY-TEST` folders instead of from the `./ALLOW` and `./DENY` folders.
- `-v`: to get verbose output, which includes the actual resquest.
- `-p`: to load another Cedar policy instead.

Request JSON files in the `./ALLOW-TEST` and `./DENY-TEST` folders are named following the convention: `<principal>-<action>-<resource>.json`.

## Graphviz Visualisation of Entities

1. Install the [`xdot`](https://github.com/jrfonseca/xdot.py) package.
On Debian, run: `sudo apt install xdot`.

2. Update the Graphviz dot file.
Run: `cedar visualize --entities childrenclinic-entities.json > childrenclinic-entities.dot`.

3. Run `xdot`, e.g.: `xdot childrenclinic-entities.dot`.

## Authorisation Model for the RSVP ChildrenClinic App

Cedar [Schema](childrenclinic.cedarschema).

### Entities

All _actors_ are either an entity `Employee` or an entity `Guest`.

Every `Employee` must have a `name`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Role` in their set `roles`.
`Role` is either `Administrator`, `Administrative Assistant`, or `Doctor`.

Optional `Employee` attributes are `level`, where entity `Level` is either `Intern`, `Resident`, `Staff`, `Senior`, `Registrar`, or `Specialist`, and `manager`, which is an entity `Employee`.
For `Level`, the values `Resident`, `Registrar`, and `Specialist` are only used in conjunction with `Role` `Doctor`.

`Guest` has no attributes.

Non-_actors_ are entities `ResponsibleAdult`, `Patient`, and `Visit`.

Every `ResponsibleAdult` must have a `name` and at least one entity `Clinic` in their set `clinics`.

Every `Patient` must have a `name`, at least one record in their set `adults` composed of an entity `ResponsibleAdult` as `adult` and of an entity `AdultAuthority` as `authority`, at least one entity `Clinic` in their set `clinics`, and at least one entity `Doctor` in their set `doctors`.
`AdultAuthority` is either `Parent`, `Legal Guardian`, `Designated Representative`, or `Authorised Adult`.

Every `Visit` must have one entity `Patient` as their `patient`, at least one entity `ResponsibleAdult` in their set `adults`, at least one entity `Clinic` in their set `clinics`, at least one entity `Doctor` in their set `doctors`, and one entity `Confidentiality` as their `confidentiality`.
`Confidentiality` is either `Official`, `Sensitive`, or `Protected`.

### Actions

Actions are separated into two groups, `EmployeeOperations` and `PatientOperations`, and every action applies to a principal whose entity is either `Employee` or `Guest`.

#### Both `EmployeeOperations` and `PatientOperations`

Action `ViewClinic` applies to a resource whose entity is `Clinic`.

#### `EmployeeOperations`

Actions `ListEmployees` and `AddEmployee` apply to a resource whose entity is `Clinic`.
Actions `ViewEmployee`, `EditEmployee`, and `DeleteEmployee` apply to a resource whose entity is `Employee`.

#### `PatientOperations`

Actions `ListPatients` and `AddPatient` apply to a resource whose entity is `Clinic`.
Actions `ViewPatient`, `EditPatient`, and `DeletePatient` apply to a resource whose entity is `Patient`.

Actions `ListAdults` and `AddAdult` apply to a resource whose entity is `Clinic`.
Actions `ViewAdult`, `EditAdult`, and `DeleteAdult` apply to a resource whose entity is `ResponsibleAdult`.

Actions `ListVisits` and `AddVisit` apply to a resource whose entity is `Clinic`.
Actions `ViewVisit`, `EditVisit`, and `DeleteVisit` apply to a resource whose entity is `Visit`.
