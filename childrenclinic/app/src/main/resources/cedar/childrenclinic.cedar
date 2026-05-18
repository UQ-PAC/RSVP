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
// Every Employee can list the Guardians, Patients, and Visits they are allowed to view.
@id("Policy: Every Employee can list Guardians, Patients, and Visits.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource is ChildrenClinic::Clinic
)
when {
    action in [ChildrenClinic::Action::"ListGuardians",
               ChildrenClinic::Action::"ListPatients",
               ChildrenClinic::Action::"ListVisits"]
};

// 'policy2'
// Anyone can view any "Doctor" or "Administrative Assistant".
@id("Policy: Public Doctor and Administrative Assistant data.")
permit (
    principal,
    action == ChildrenClinic::Action::"ViewEmployee",
    resource is ChildrenClinic::Employee
)
when {
    resource.roles.containsAny([ChildrenClinic::EmployeeRole::"Administrative Assistant",
                                ChildrenClinic::EmployeeRole::"Doctor"])
};

// 'policy3'
// No Guest can access any Patient data.
@id("Policy: Guest cannot access Patient data.")
forbid (
    principal is ChildrenClinic::Guest,
    action in ChildrenClinic::Action::"PatientOperations",
    resource
)
when {
    resource is ChildrenClinic::Clinic ||
    resource is ChildrenClinic::Patient ||
    resource is ChildrenClinic::Guardian ||
    resource is ChildrenClinic::Visit
};

// 'policy4'
// Every "Administrator" can do anything, anywhere.
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
// Every "Administrative Assistant" can view and edit their Clinics' data.
@id("Policy: Administrative Assistant can view and edit in their Clinics.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Administrative Assistant") &&
    (
        resource is ChildrenClinic::Employee ||
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Guardian ||
        resource is ChildrenClinic::Visit
    ) &&
    resource.clinics.containsAny(principal.clinics)
}
unless {
    action in [ChildrenClinic::Action::"AddGuardian",
               ChildrenClinic::Action::"AddEmployee",
               ChildrenClinic::Action::"AddPatient",
               ChildrenClinic::Action::"AddVisit",
               ChildrenClinic::Action::"DeleteGuardian",
               ChildrenClinic::Action::"DeleteEmployee",
               ChildrenClinic::Action::"DeletePatient",
               ChildrenClinic::Action::"DeleteVisit"] ||
    (
        resource has roles &&
        resource.roles.contains(ChildrenClinic::EmployeeRole::"Administrator")
    )
};

// 'policy6'
// Every "Administrative Assistant" can add Patients and Guardians to their Clinics' data.
@id("Policy: Administrative Assistant can add Patients and Guardians to their Clinics.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource is ChildrenClinic::Clinic
)
when {
    action in [ChildrenClinic::Action::"AddGuardian",
               ChildrenClinic::Action::"AddPatient"] &&
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Administrative Assistant") &&
    principal.clinics.contains(resource)
};

// 'policy7'
// Every "Doctor" can view and edit their Patients' data.
@id("Policy: Doctor can view and edit their Patients.")
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
    action in [ChildrenClinic::Action::"DeletePatient",
               ChildrenClinic::Action::"DeleteVisit"]
};

// 'policy8'
// Every "Doctor" can add Guardians and Patients to their Clinics' data.
@id("Policy: Doctor can add Guardians and Patients to their Clinics.")
permit (
    principal is ChildrenClinic::Employee,
    action,
    resource is ChildrenClinic::Clinic
)
when {
    action in [ChildrenClinic::Action::"AddGuardian",
               ChildrenClinic::Action::"AddPatient"] &&
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    principal.clinics.contains(resource)
}
unless {
    principal has level &&
    principal.level == ChildrenClinic::ProfessionalLevel::"Intern"
};

// 'policy9'
// No "Intern" "Doctor" can edit their Patients' data.
@id("Policy: Intern Doctor cannot edit their Patients.")
forbid (
    principal is ChildrenClinic::Employee,
    action,
    resource
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    principal has level &&
    principal.level == ChildrenClinic::ProfessionalLevel::"Intern" &&
    action in [ChildrenClinic::Action::"EditPatient",
               ChildrenClinic::Action::"EditVisit"] &&
    (
        resource is ChildrenClinic::Patient ||
        resource is ChildrenClinic::Visit
    ) &&
    principal in resource.doctors
};

// 'policy10'
// Every Employee can view and edit their own data.
@id("Policy: Employee can view and edit their own data.")
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
// Every "Senior" or "Specialist" "Doctor" can delete their Patients' data.
@id("Policy: Senior or Specialist Doctor can delete their Patients.")
permit (
    principal is ChildrenClinic::Employee,
    action,
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
    principal in resource.doctors &&
    action in [ChildrenClinic::Action::"DeletePatient",
               ChildrenClinic::Action::"DeleteVisit"]
};

// 'policy12'
// Every "Senior" or "Specialist" "Doctor" can view all Patients' data.
@id("Policy: Senior or Specialist Doctor can view all Patients.")
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
        resource.roles.containsAny([ChildrenClinic::EmployeeRole::"Administrative Assistant",
                                    ChildrenClinic::EmployeeRole::"Doctor"])
    )
};

// 'policy14'
// Every Employee can view the Clinics they are assigned to.
@id("Policy: Every Employee can view the Clinics they are assigned to.")
permit (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"ViewClinic",
    resource is ChildrenClinic::Clinic
)
when {
    principal.clinics.contains(resource)
};

// 'policy15'
// Every Doctor can view Guardians in their Clinics.
@id("Policy: Doctor can view Guardians in their Clinics.")
permit (
    principal is ChildrenClinic::Employee,
    action == ChildrenClinic::Action::"ViewGuardian",
    resource is ChildrenClinic::Guardian
)
when {
    principal.roles.contains(ChildrenClinic::EmployeeRole::"Doctor") &&
    resource.clinics.containsAny(principal.clinics)
};
