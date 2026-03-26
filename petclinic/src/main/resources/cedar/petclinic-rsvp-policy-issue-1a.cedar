// PetClinic RSVP Policy - Issue 1a
// Introduces expansion of 'policy3' due to misused logical operator.

// 'policy0'
// Anyone can view any Secretary or Veterinarian.
@id("Policy: Public Secretary and Veterinarian data.")
permit (
    principal,
    action == PetClinic::Action::"ViewEmployee",
    resource is PetClinic::Employee
)
when {
    resource.roles.containsAny([PetClinic::EmployeeRole::"Secretary",
                                PetClinic::EmployeeRole::"Veterinarian"])
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
    resource is PetClinic::PetOwner ||
    resource is PetClinic::Pet ||
    resource is PetClinic::PetVisit
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
    principal.roles.contains(PetClinic::EmployeeRole::"Administrator")
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
    principal.roles.contains(PetClinic::EmployeeRole::"Secretary") &&
    (
        resource is PetClinic::Employee ||
        resource is PetClinic::PetOwner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::PetVisit
    ) &&
    resource.clinics.containsAny(principal.clinics)
}
unless {
    action in [PetClinic::Action::"AddClient",
               PetClinic::Action::"AddEmployee",
               PetClinic::Action::"DeleteClient",
               PetClinic::Action::"DeleteEmployee"] && // Should have been "||".
    (
        resource has roles &&
        resource.roles.contains(PetClinic::EmployeeRole::"Administrator")
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
    principal.roles.contains(PetClinic::EmployeeRole::"Secretary") &&
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
    principal.roles.contains(PetClinic::EmployeeRole::"Veterinarian") &&
    (
        resource is PetClinic::PetOwner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::PetVisit
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
    principal.roles.contains(PetClinic::EmployeeRole::"Veterinarian") &&
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
    principal.roles.contains(PetClinic::EmployeeRole::"Veterinarian") &&
    principal has level &&
    principal.level == PetClinic::ProfessionalLevel::"Intern" &&
    (
        resource is PetClinic::PetOwner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::PetVisit
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
    principal.roles.contains(PetClinic::EmployeeRole::"Veterinarian") &&
    principal has level &&
    (
        principal.level == PetClinic::ProfessionalLevel::"Senior" ||
        principal.level == PetClinic::ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is PetClinic::PetOwner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::PetVisit
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
    principal.roles.contains(PetClinic::EmployeeRole::"Veterinarian") &&
    principal has level &&
    (
        principal.level == PetClinic::ProfessionalLevel::"Senior" ||
        principal.level == PetClinic::ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is PetClinic::PetOwner ||
        resource is PetClinic::Pet ||
        resource is PetClinic::PetVisit
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
    principal.roles.contains(PetClinic::EmployeeRole::"Administrator")
};
