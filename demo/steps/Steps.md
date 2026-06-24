## Steps of the Childrens Clinic demo

- Upload initial entities.json and schema.cedarschema
- Upload policy-0.cedar: Unused policy warning
- Upload policy-1.cedar: Contradicted policy warning
- Upload policy-2.cedar: Duplicated policy warning
- Upload policy-3.cedar: No warning
- Upload policy-4.cedar: Subsumed policy warning, change inpact
- Upload policy.invariants: Invariants do not hold
- Upload polyci.5.cedar: No warning, invariants hold
- Upload policy-6.cedar: Change impact (permissions added)
- Upload policy-7.cedar: Change impact removed

Additional step: demonstrate structural invariants:
- Remove entities.json
- Remove Administrator entity from entities.json and re-upload.
- Invariant 1 should be violated
- Bring entity back and re-upload: invariant holds
