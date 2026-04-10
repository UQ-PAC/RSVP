// ChildClinic RSVP Policy
// Original version without introduced issues.

// 'policy0'
// Anyone can list the Employees they are allowed to view.
@id("Policy: Anyone can list Employees.")
permit (
    principal,
    action == ChildClinic::Action::"ListEmployees",
    resource is ChildClinic::Clinic
);

// 'policy1'
// Every Employee can list the Clients they are allowed to view.
@id("Policy: Every Employee can list Clients.")
permit (
    principal is ChildClinic::Employee,
    action == ChildClinic::Action::"ListClients",
    resource is ChildClinic::Clinic
);

// 'policy2'
// Anyone can view any Secretary or Doctor.
@id("Policy: Public Secretary and Doctor data.")
permit (
    principal,
    action == ChildClinic::Action::"ViewEmployee",
    resource is ChildClinic::Employee
)
when {
    resource.roles.containsAny([ChildClinic::EmployeeRole::"Secretary",
                                ChildClinic::EmployeeRole::"Doctor"])
};

// 'policy3'
// No Guest can access any client data.
@id("Policy: Guest cannot access client data.")
forbid (
    principal is ChildClinic::Guest,
    action in ChildClinic::Action::"ClientOperations",
    resource
)
when {
    resource is ChildClinic::Parent ||
    resource is ChildClinic::Child ||
    resource is ChildClinic::Visit
};

// 'policy4'
// Every Administrator can do anything, anywhere.
@id("Policy: Administrator.")
permit (
    principal is ChildClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Administrator")
};

// 'policy5'
// Every Secretary can view and edit their Clinics' data.
@id("Policy: Secretary can view and edit in their Clinics.")
permit (
    principal is ChildClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Secretary") &&
    (
        resource is ChildClinic::Employee ||
        resource is ChildClinic::Parent ||
        resource is ChildClinic::Child ||
        resource is ChildClinic::Visit
    ) &&
    resource.clinics.containsAny(principal.clinics)
}
unless {
    action in [ChildClinic::Action::"AddClient",
               ChildClinic::Action::"AddEmployee",
               ChildClinic::Action::"DeleteClient",
               ChildClinic::Action::"DeleteEmployee"] ||
    (
        resource has roles &&
        resource.roles.contains(ChildClinic::EmployeeRole::"Administrator")
    )
};

// 'policy6'
// Every Secretary can add clients to their Clinics' data.
@id("Policy: Secretary can add clients to their Clinics.")
permit (
    principal is ChildClinic::Employee,
    action == ChildClinic::Action::"AddClient",
    resource is ChildClinic::Clinic
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Secretary") &&
    principal.clinics.contains(resource)
};

// 'policy7'
// Every Doctor can view and edit their clients' data.
@id("Policy: Doctor can view and edit their clients.")
permit (
    principal is ChildClinic::Employee,
    action in ChildClinic::Action::"ClientOperations",
    resource
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Doctor") &&
    (
        resource is ChildClinic::Parent ||
        resource is ChildClinic::Child ||
        resource is ChildClinic::Visit
    ) &&
    principal in resource.doctors
}
unless {
    action == ChildClinic::Action::"DeleteClient"
};

// 'policy8'
// Every Doctor can add their clients' data.
@id("Policy: Doctor can add their clients.")
permit (
    principal is ChildClinic::Employee,
    action == ChildClinic::Action::"AddClient",
    resource is ChildClinic::Clinic
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Doctor") &&
    principal.clinics.contains(resource)
};

// 'policy9'
// No Intern Doctor can edit their clients' data.
@id("Policy: Intern Doctor cannot edit their clients.")
forbid (
    principal is ChildClinic::Employee,
    action == ChildClinic::Action::"EditClient",
    resource
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    principal.level == ChildClinic::ProfessionalLevel::"Intern" &&
    (
        resource is ChildClinic::Parent ||
        resource is ChildClinic::Child ||
        resource is ChildClinic::Visit
    ) &&
    principal in resource.doctors
};

// 'policy10'
// Every Employee can view and edit their own data.
@id("Policy: Employee can edit their own data.")
permit (
    principal is ChildClinic::Employee,
    action,
    resource is ChildClinic::Employee
)
when {
    action in [ChildClinic::Action::"ViewEmployee",
               ChildClinic::Action::"EditEmployee"] &&
    principal == resource
};

// 'policy11'
// Every Senior or Specialist Doctor can delete their clients' data.
@id("Policy: Senior or Specialist Doctor can delete their clients.")
permit (
    principal is ChildClinic::Employee,
    action in ChildClinic::Action::"DeleteClient",
    resource
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    (
        principal.level == ChildClinic::ProfessionalLevel::"Senior" ||
        principal.level == ChildClinic::ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is ChildClinic::Parent ||
        resource is ChildClinic::Child ||
        resource is ChildClinic::Visit
    ) &&
    principal in resource.doctors
};

// 'policy12'
// Every Senior or Specialist Doctor can view all clients' data.
@id("Policy: Senior or Specialist Doctor can view all clients.")
permit (
    principal is ChildClinic::Employee,
    action == ChildClinic::Action::"ViewClient",
    resource
)
when {
    principal.roles.contains(ChildClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    (
        principal.level == ChildClinic::ProfessionalLevel::"Senior" ||
        principal.level == ChildClinic::ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is ChildClinic::Parent ||
        resource is ChildClinic::Child ||
        resource is ChildClinic::Visit
    )
}
unless {
    resource has confidentiality &&
    resource.confidentiality == ChildClinic::Confidentiality::"Protected"
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
        principal is ChildClinic::Employee &&
        principal.roles.contains(ChildClinic::EmployeeRole::"Administrator")
    ) ||
    (
        action == ChildClinic::Action::"ViewEmployee" &&
        resource.roles.containsAny([ChildClinic::EmployeeRole::"Secretary",
                                    ChildClinic::EmployeeRole::"Doctor"])
    )
};
