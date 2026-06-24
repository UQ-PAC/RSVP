# OpenFGA Authorization Files

This folder contains the OpenFGA files for the RSVP version of PetClinic, ChildrenClinic:
- DSL Model: `childrenclinic.fga`
- Store: `childrenclinic.fga.yaml`

To run all tests: `fga model test --verbose --tests childrenclinic.fga.yaml`.

To only validate (implicit in `fga model test`): `fga model validate --file childrenclinic.fga`.

Note: `fga store import --file childrenclinic.fga.yaml` provisions both the model and the tuples in a single command against a running OpenFGA server.

## Graphviz Visualisation of Entities

1. Install the [`xdot`](https://github.com/jrfonseca/xdot.py) package.
On Debian, run: `sudo apt install xdot`.

2. Create or update a Graphviz dot file.
See [FGA Visualize](#fga-visualize).

3. Run `xdot`, e.g.: `xdot <file_name>.dot`.
For better visualisation, consider running `xdot -f fdp <file_name>.dot` or `xdot -f circo <file_name>.dot`.

### FGA Visualize

The `fga_visualize.py` script reads the `childrenclinic.fga.yaml` store file and outputs DOT with the following mapping:

| Cedar `visualize` | `fga_visualize.py` |
|---|---|
| Entity: node | Object: node |
| Type: subgraph cluster | Type: subgraph cluster |
| `parents`: unlabelled edge | Relation tuple: labelled edge |
| No filtering | `--types`, `--relations`, `--exclude-types`, `--exclude-relations`, `--no-wildcards` |

Filtering is essential because the OpenFGA tuple graph is far denser than a typical Cedar entity graph (which has only `parents` edges).

Output examples:
- `authorization_graph.dot`: the core authorisation-relevant relations across all domain types, excluding informational relations (`role`, `level`, `manager`, `confidentiality`, `self`, `public_viewable`, `is_administrator`) and the enum/junction types.
- `clinic_membership.dot`: clinic-scoped role assignments (member, doctor, receptionist, administrator, etc.), excluding the group infrastructure and wildcard tuples.
- `patient_access.dot`: patient and visit entities, filtered to `clinic`, `assigned_doctor`, `intern_assigned_doctor`, `senior_or_specialist_assigned_doctor`, and `confidentiality_protected` relations only.

Usage:

```bash
# DOT to stdout (pipe to dot or copy to a .dot file).
python3 fga_visualize.py childrenclinic.fga.yaml

# Render directly to SVG.
python3 fga_visualize.py childrenclinic.fga.yaml -o graph.svg

# Focused view: only patient/visit relations for a specific relation set.
python3 fga_visualize.py childrenclinic.fga.yaml \
  --types patient,visit,clinic,user \
  --relations clinic,assigned_doctor \
  -o patient_doctors.svg

# `authorization_graph.dot`.
python3 fga_visualize.py childrenclinic.fga.yaml \
  --exclude-types employee_role,professional_level,confidentiality,guardian_authority,patient_guardian,guest \
  --exclude-relations can_list_employees,can_list_patients,can_list_guardians,role,level,manager,is_public_viewable,is_administrator,self,confidentiality \
  --no-wildcards \
  --rankdir TB \
  -o authorization_graph.dot

# `clinic_membership.dot`.
python3 fga_visualize.py childrenclinic.fga.yaml \
  --types clinic,employee,guest \
  --exclude-relations can_list_employees,can_list_patients,can_list_guardians \
  --rankdir TB \
  -o clinic_membership.dot

# `patient_access.dot`.
python3 fga_visualize.py childrenclinic.fga.yaml \
  --types patient,visit,clinic,employee \
  --relations clinic,assigned_doctor,intern_assigned_doctor,senior_or_specialist_assigned_doctor,confidentiality_protected \
  --rankdir TB \
  -o patient_access.dot
```

The only external dependency is PyYAML (for parsing the store file).
Graphviz (`dot`) is required only when rendering to SVG/PNG/PDF; the `--output file.dot` option bypasses it entirely.

### Model DSL to JSON

`fga model transform --file childrenclinic.fga > childrenclinic.json`

The output is the JSON structure accepted by the `WriteAuthorizationModel` API endpoint and by the `dev.openfga:openfga-sdk` Java client's `ClientWriteAuthorizationModelRequest`.

### Store file (tuples) to JSON

```bash
python3 -c "
import yaml, json
with open('childrenclinic.fga.yaml') as f:
    data = yaml.safe_load(f)
tuples = [{'user': t['user'], 'relation': t['relation'], 'object': t['object']}
          for t in data['tuples']]
print(json.dumps({'writes': {'tuple_keys': tuples}}, indent=2))
" > tuples.json
```

The output is the JSON structure accepted by the `Write` API.

## Authorisation Model for the RSVP ChildrenClinic App

OpenFGA [Model](childrenclinic.fga).
