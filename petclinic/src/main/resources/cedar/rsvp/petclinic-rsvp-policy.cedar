// PetClinic RSVP Policy
// Original version without introduced issues.

// 'policy0'
// Anyone can view any Secretary or Veterinarian.
@id("Policy: Public Secretary and Veterinarian data.")
permit (
    principal,
    action == PetClinic::Action::"ViewEmployee",
    resource is PetClinic::Employee
)
when {
    resource.roles.containsAny([PetClinic::Role::"Secretary",
                                PetClinic::Role::"Veterinarian"])
};

// 'policy1'
// No Guest can access any client data.
@id("Policy: Guest cannot access client data.")
forbid (
    principal is PetClinic::Guest,
    action in PetClinic::Action::"ClientOperations",
    resource
)
when {
    resource is PetClinic::Owner ||
    resource is PetClinic::Pet ||
    resource is PetClinic::Visit
};

// 'policy2'
// Every Administrator can do anything, anywhere.
@id("Policy: Administrator.")
permit (
    principal is PetClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(PetClinic::Role::"Administrator")
};

// 'policy3'
// Every Secretary can view and edit their Clinics' data.
@id("Policy: Secretary can view and edit in their Clinics.")
permit (
    principal is PetClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(PetClinic::Role::"Secretary") &&
    (
        resource is PetClinic::Employee ||
        resource is PetClinic::Owner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::Visit
    ) &&
    resource.clinics.containsAny(principal.clinics)
}
unless {
    action in [PetClinic::Action::"AddClient", PetClinic::Action::"AddEmployee",
               PetClinic::Action::"DeleteClient", PetClinic::Action::"DeleteEmployee"] ||
    (
        resource has roles &&
        resource.roles.contains(PetClinic::Role::"Administrator")
    )
};

// 'policy4'
// Every Secretary can add clients to their Clinics' data.
@id("Policy: Secretary can add clients to their Clinics.")
permit (
    principal is PetClinic::Employee,
    action == PetClinic::Action::"AddClient",
    resource is PetClinic::Clinic
)
when {
    principal.roles.contains(PetClinic::Role::"Secretary") &&
    principal.clinics.contains(resource)
};

// 'policy5'
// Every Veterinarian can view and edit their clients' data.
@id("Policy: Veterinarian can view and edit their clients.")
permit (
    principal is PetClinic::Employee,
    action in PetClinic::Action::"ClientOperations",
    resource
)
when {
    principal.roles.contains(PetClinic::Role::"Veterinarian") &&
    (
        resource is PetClinic::Owner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::Visit
    ) &&
    principal in resource.veterinarians
}
unless {
    action == PetClinic::Action::"DeleteClient"
};

// 'policy6'
// Every Veterinarian can add their clients' data.
@id("Policy: Veterinarian can add their clients.")
permit (
    principal is PetClinic::Employee,
    action == PetClinic::Action::"AddClient",
    resource is PetClinic::Clinic
)
when {
    principal.roles.contains(PetClinic::Role::"Veterinarian") &&
    principal.clinics.contains(resource)
};

// 'policy7'
// No Intern Veterinarian can edit their clients' data.
// > > Could conflict with 'policy8'.
@id("Policy: Intern Veterinarian cannot edit their clients.")
forbid (
    principal is PetClinic::Employee,
    action == PetClinic::Action::"EditClient",
    resource
)
when {
    principal.roles.contains(PetClinic::Role::"Veterinarian") &&
    principal has level &&
    principal.level == PetClinic::Level::"Intern" &&
    (
        resource is PetClinic::Owner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::Visit
    ) &&
    principal in resource.veterinarians
};

// 'policy8'
// Every Employee can edit their own data.
// > > Could conflict with 'policy7'.
@id("Policy: Employee can edit their own data.")
permit (
    principal is PetClinic::Employee,
    action == PetClinic::Action::"EditEmployee",
    resource is PetClinic::Employee
)
when {
    principal == resource
};

// 'policy9'
// Every Senior or Specialist Veterinarian can delete their clients' data.
@id("Policy: Senior or Specialist Veterinarian can delete their clients.")
permit (
    principal is PetClinic::Employee,
    action in PetClinic::Action::"DeleteClient",
    resource
)
when {
    principal.roles.contains(PetClinic::Role::"Veterinarian") &&
    principal has level &&
    (
        principal.level == PetClinic::Level::"Senior" ||
        principal.level == PetClinic::Level::"Specialist"
    ) &&
    (
        resource is PetClinic::Owner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::Visit
    ) &&
    principal in resource.veterinarians
};

// 'policy10'
// Every Senior or Specialist Veterinarian can view all clients' data.
@id("Policy: Senior or Specialist Veterinarian can view all clients.")
permit (
    principal is PetClinic::Employee,
    action == PetClinic::Action::"ViewClient",
    resource
)
when {
    principal.roles.contains(PetClinic::Role::"Veterinarian") &&
    principal has level &&
    (
        principal.level == PetClinic::Level::"Senior" ||
        principal.level == PetClinic::Level::"Specialist"
    ) &&
    (
        resource is PetClinic::Owner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::Visit
    )
}
unless {
    resource has confidentiality &&
    resource.confidentiality == PetClinic::Confidentiality::"Protected"
};

// 'policy11'
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
    principal is PetClinic::Employee &&
    principal.roles.contains(PetClinic::Role::"Administrator")
};

// 'policy12'
// No Employee can be their own manager.

// 'policy13'
// Level with values Resident, Registrar, and Specialist are only used
// in conjunction with Role Veterinarian.
