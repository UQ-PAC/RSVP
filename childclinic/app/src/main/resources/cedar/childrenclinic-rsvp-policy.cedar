// ChildrenClinic RSVP Policy
// Original version without introduced issues.

// 'policy0'
// Anyone can list the Employees they are allowed to view.
@id("Policy: Anyone can list Employees.")
permit (
    principal,
    action == ChildrenClinic::Action::"ListEmployees",
    resource is ChildrenClinic::Clinic
);

// 'policy1'
// Every Employee can list the Patients they are allowed to view.
@id("Policy: Every Employee can list Patients.")
permit (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"ListPatients",
    resource is ChildrenClinic::Clinic
);

// 'policy2'
// Anyone can view any Secretary or Doctor.
@id("Policy: Public Secretary and Doctor data.")
permit (
    principal,
    action == ChildrenClinic::Action::"ViewEmployee",
    resource is ChildrenClinic::Employee
)
when {
    resource.roles.containsAny([ChildrenClinic::EmployeeRole::"Secretary",
                                ChildrenClinic::EmployeeRole::"Doctor"])
};

// 'policy3'
// No Guest can access any patient data.
@id("Policy: Guest cannot access patient data.")
forbid (
    principal is ChildrenClinic::Guest,
    action in ChildrenClinic::Action::"PatientOperations",
    resource
)
when {
    resource is ChildrenClinic::Patient ||
    resource is ChildrenClinic::Visit
};

// 'policy4'
// Every Administrator can do anything, anywhere.
@id("Policy: Administrator.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Administrator")
};

// 'policy5'
// Every Secretary can view and edit their Clinics' data.
@id("Policy: Secretary can view and edit in their Clinics.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Secretary") &&
    (
        resource is ChildrenClinic::Employee ||
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Visit
    ) &&
    resource.clinics.containsAny(principal.clinics)
}
unless {
    action in [ChildrenClinic::Action::"AddPatient",
               ChildrenClinic::Action::"AddEmployee",
               ChildrenClinic::Action::"DeletePatient",
               ChildrenClinic::Action::"DeleteEmployee"] ||
    (
        resource has roles &&
        resource.roles.contains(ChildrenClinic::EmployeeRole::"Administrator")
    )
};

// 'policy6'
// Every Secretary can add patients to their Clinics' data.
@id("Policy: Secretary can add patients to their Clinics.")
permit (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"AddPatient",
    resource is ChildrenClinic::Clinic
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Secretary") &&
    principal.clinics.contains(resource)
};

// 'policy7'
// Every Doctor can view and edit their patients' data.
@id("Policy: Doctor can view and edit their patients.")
permit (
    principal is ChildrenClinic::Employee,
    action in ChildrenClinic::Action::"PatientOperations",
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    (
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Visit
    ) &&
    principal in resource.doctors
}
unless {
    action == ChildrenClinic::Action::"DeletePatient"
};

// 'policy8'
// Every Doctor can add their patients' data.
@id("Policy: Doctor can add their patients.")
permit (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"AddPatient",
    resource is ChildrenClinic::Clinic
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    principal.clinics.contains(resource)
};

// 'policy9'
// No Intern Doctor can edit their patients' data.
@id("Policy: Intern Doctor cannot edit their patients.")
forbid (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"EditPatient",
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    principal.level == ChildrenClinic::ProfessionalLevel::"Intern" &&
    (
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Visit
    ) &&
    principal in resource.doctors
};

// 'policy10'
// Every Employee can view and edit their own data.
@id("Policy: Employee can edit their own data.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource is ChildrenClinic::Employee
)
when {
    action in [ChildrenClinic::Action::"ViewEmployee",
               ChildrenClinic::Action::"EditEmployee"] &&
    principal == resource
};

// 'policy11'
// Every Senior or Specialist Doctor can delete their patients' data.
@id("Policy: Senior or Specialist Doctor can delete their patients.")
permit (
    principal is ChildrenClinic::Employee,
    action in ChildrenClinic::Action::"DeletePatient",
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    (
        principal.level == ChildrenClinic::ProfessionalLevel::"Senior" ||
        principal.level == ChildrenClinic::ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Visit
    ) &&
    principal in resource.doctors
};

// 'policy12'
// Every Senior or Specialist Doctor can view all patients' data.
@id("Policy: Senior or Specialist Doctor can view all patients.")
permit (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"ViewPatient",
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    (
        principal.level == ChildrenClinic::ProfessionalLevel::"Senior" ||
        principal.level == ChildrenClinic::ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Visit
    )
}
unless {
    resource has confidentiality &&
    resource.confidentiality == ChildrenClinic::Confidentiality::"Protected"
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
        principal is ChildrenClinic::Employee &&
        principal.roles.contains(ChildrenClinic::EmployeeRole::"Administrator")
    ) ||
    (
        action == ChildrenClinic::Action::"ViewEmployee" &&
        resource.roles.containsAny([ChildrenClinic::EmployeeRole::"Secretary",
                                    ChildrenClinic::EmployeeRole::"Doctor"])
    )
};
