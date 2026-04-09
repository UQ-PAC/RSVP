// PetClinic RSVP Policy
// Original version without introduced issues.

// 'policy0'
// Anyone can list the Employees they are allowed to view.
@id("Policy: Anyone can list Employees.")
permit (
    principal,
    action == PetClinic::Action::"ListEmployees",
    resource is PetClinic::Clinic
);

// 'policy1'
// Every Employee can list the Clients they are allowed to view.
@id("Policy: Every Employee can list Clients.")
permit (
    principal is PetClinic::Employee,
    action == PetClinic::Action::"ListClients",
    resource is PetClinic::Clinic
);

// 'policy2'
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

// 'policy3'
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

// 'policy4'
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

// 'policy5'
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
               PetClinic::Action::"DeleteEmployee"] ||
    (
        resource has roles &&
        resource.roles.contains(PetClinic::EmployeeRole::"Administrator")
    )
};

// 'policy6'
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

// 'policy7'
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

// 'policy8'
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

// 'policy9'
// No Intern Veterinarian can edit their clients' data.
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

// 'policy10'
// Every Employee can view and edit their own data.
@id("Policy: Employee can edit their own data.")
permit (
    principal is PetClinic::Employee,
    action,
    resource is PetClinic::Employee
)
when {
    action in [PetClinic::Action::"ViewEmployee",
               PetClinic::Action::"EditEmployee"] &&
    principal == resource
};

// 'policy11'
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

// 'policy12'
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

// 'policy13'
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
    (
        principal is PetClinic::Employee &&
        principal.roles.contains(PetClinic::EmployeeRole::"Administrator")
    ) ||
    (
        action == PetClinic::Action::"ViewEmployee" &&
        resource.roles.containsAny([PetClinic::EmployeeRole::"Secretary",
                                    PetClinic::EmployeeRole::"Veterinarian"])
    )
};
