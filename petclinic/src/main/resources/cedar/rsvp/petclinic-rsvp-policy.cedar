// 'policy0'
// Anyone can view any Veterinarian.
// > > Could conflict with 'policy2'.
@id("Policy: Public Veterinarian data.")
permit (
    principal,
    action == PetClinic::Action::"ViewVeterinarian",
    resource is PetClinic::Veterinarian
);

// 'policy1'
// Anyone can view any Secretary.
// > > Could conflict with 'policy2'.
@id("Policy: Public Secretary data.")
permit (
    principal,
    action == PetClinic::Action::"ViewSecretary",
    resource is PetClinic::Secretary
);

// 'policy2'
// No Guest can view any customer data.
// > > Could conflict with 'policy0' and 'policy1'.
@id("Policy: Guest cannot view customer data.")
forbid (
    principal is PetClinic::Guest,
    action,
    resource
)
unless {
    (
        action == PetClinic::Action::"ViewVeterinarian" &&
        resource is PetClinic::Veterinarian
    ) ||
    (
        action == PetClinic::Action::"ViewSecretary" &&
        resource is PetClinic::Secretary
    )
};

// 'policy3'
// Every Administrator can do anything, anywhere.
@id("Policy: Administrator.")
permit (
    principal is PetClinic::Administrator,
    action,
    resource
);

// 'policy4'
// Any Secretary can view and edit their Clinics' data.
@id("Policy: Secretary can view and edit in their Clinics.")
permit (
    principal is PetClinic::Secretary,
    action,
    resource
)
when {
    resource has clinics &&
    resource.clinics.containsAny(principal.clinics)
}
unless {
    action in PetClinic::Action::"DeleteOperations"
};

// 'policy5'
// Every Veterinarian can view and edit their clients' data.
@id("Policy: Veterinarian can view and edit their clients.")
permit (
    principal is PetClinic::Veterinarian,
    action,
    resource
)
when {
    resource has veterinarians &&
    principal in resource.veterinarians &&
    (
        action in PetClinic::Action::"OwnerOperations" ||
        action in PetClinic::Action::"PetOperations" ||
        action in PetClinic::Action::"VisitOperations"
    )
}
unless {
    action in PetClinic::Action::"DeleteOperations"
};

// 'policy6'
// No Intern Veterinarian can edit their clients' data.
// > > Could have unintended consequences since "EditOperations"
// > > is also used for "VeterinarianOperations".
@id("Policy: Intern Veterinarian cannot edit their clients.")
forbid (
    principal is PetClinic::Veterinarian,
    action in PetClinic::Action::"EditOperations",
    resource
)
when {
    principal.level == PetClinic::Level::"Intern"
};

// 'policy7'
// Only a Senior or Specialist Veterinarian can delete their clients' data.
@id("Policy: Senior or Specialist Veterinarian can delete their clients.")
permit (
    principal is PetClinic::Veterinarian,
    action in PetClinic::Action::"DeleteOperations",
    resource
)
when {
    resource has veterinarians &&
    principal in resource.veterinarians &&
    (
        principal.level == PetClinic::Level::"Senior" ||
        principal.level == PetClinic::Level::"Specialist"
    ) &&
    (
        action in PetClinic::Action::"OwnerOperations" ||
        action in PetClinic::Action::"PetOperations" ||
        action in PetClinic::Action::"VisitOperations"
    )
};

// 'policy8'
// No Principal can view data from a Clinic they are not assigned to.
@id("Policy: Principal cannot view data from another Clinic.")
forbid (
    principal,
    action,
    resource
)
when {
    principal has clinics &&
    resource has clinics &&
    !resource.clinics.containsAny(principal.clinics)
}
unless {
    principal is PetClinic::Administrator
};
