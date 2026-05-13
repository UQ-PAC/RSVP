// 'policy0'
// Anyone can list the Employees they are allowed to view.
@id("Policy: Anyone can list Employees.")
permit (
    principal,
    action == Action::"ListEmployees",
    resource is Clinic
);

// 'policy1'
// Every Employee can list the Adults, Patients, and Visits they are allowed to view.
@id("Policy: Every Employee can list Adults, Patients, and Visits.")
permit (
    principal is Employee,
    action in [Action::"ListAdults", Action::"ListPatients", Action::"ListVisits"],
    resource is Clinic
);

// 'policy2'
// Anyone can view any Doctor or Administrative Assistant.
@id("Policy: Public Doctor and Administrative Assistant data.")
permit (
    principal,
    action == Action::"ViewEmployee",
    resource is Employee
)
when {
    resource.roles.containsAny([EmployeeRole::"Administrative Assistant",
                                EmployeeRole::"Doctor"])
};

// 'policy3'
// No Guest can access any Patient data.
@id("Policy: Guest cannot access Patient data.")
forbid (
    principal is Guest,
    action in Action::"PatientOperations",
    resource
)
when {
    resource is Clinic ||
    resource is Patient ||
    resource is ResponsibleAdult ||
    resource is Visit
};

// 'policy4'
// Every Administrator can do anything, anywhere.
@id("Policy: Administrator.")
permit (
    principal is Employee,
    action,
    resource
)
when {
    principal.roles.contains(EmployeeRole::"Administrator")
};

// 'policy5'
// Every Administrative Assistant can view and edit their Clinics' data.
@id("Policy: Administrative Assistant can view and edit in their Clinics.")
permit (
    principal is Employee,
    action,
    resource
)
when {
    principal.roles.contains(EmployeeRole::"Administrative Assistant") &&
    resource.clinics.containsAny(principal.clinics)
};

// 'policy6'
// Every Administrative Assistant can add Patients and Adults to their Clinics' data.
@id("Policy: Administrative Assistant can add Patients and Adults to their Clinics.")
permit (
    principal is Employee,
    action in [Action::"AddAdult", Action::"AddPatient"],
    resource is Clinic
)
when {
    principal.roles.contains(EmployeeRole::"Administrative Assistant") &&
    principal.clinics.contains(resource)
};

// 'policy7'
// Every Doctor can view and edit their Patients' data.
@id("Policy: Doctor can view and edit their Patients.")
permit (
    principal is Employee,
    action in Action::"PatientOperations",
    resource
)
when {
    principal.roles.contains(EmployeeRole::"Doctor") &&
    (
        resource is Patient ||
        resource is Visit
    ) &&
    principal in resource.doctors
}
unless {
    action in [Action::"DeletePatient",
               Action::"DeleteVisit"]
};

// 'policy8'
// Every Doctor can add Adults and Patients to their Clinics' data.
@id("Policy: Doctor can add Adults and Patients to their Clinics.")
permit (
    principal is Employee,
    action in [Action::"AddAdult", Action::"AddPatient"],
    resource is Clinic
)
when {
    principal.roles.contains(EmployeeRole::"Doctor") &&
    principal.clinics.contains(resource)
}
unless {
    principal has level &&
    principal.level == ProfessionalLevel::"Intern"
};

// 'policy9'
// No Intern Doctor can edit their Patients' data.
@id("Policy: Intern Doctor cannot edit their Patients.")
forbid (
    principal is Employee,
    action == Action::"EditPatient",
    resource
)
when {
    principal.roles.contains(EmployeeRole::"Doctor") &&
    principal has level &&
    principal.level == ProfessionalLevel::"Intern" &&
    (
        resource is Patient ||
        resource is Visit
    ) &&
    principal in resource.doctors
};

// 'policy10'
// Every Employee can view and edit their own data.
@id("Policy: Employee can view and edit their own data.")
permit (
    principal is Employee,
    action in [Action::"ViewEmployee", Action::"EditEmployee"],
    resource is Employee
)
when {
    principal == resource
};

// 'policy11'
// Every Senior or Specialist Doctor can delete their Patients' data.
@id("Policy: Senior or Specialist Doctor can delete their Patients.")
permit (
    principal is Employee,
    action in [Action::"DeletePatient", Action::"DeleteVisit"],
    resource
)
when {
    principal.roles.contains(EmployeeRole::"Doctor") &&
    principal has level &&
    (
        principal.level == ProfessionalLevel::"Senior" ||
        principal.level == ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is Patient ||
        resource is Visit
    ) &&
    principal in resource.doctors
};

// 'policy12'
// Every Senior or Specialist Doctor can view all Patients' data.
@id("Policy: Senior or Specialist Doctor can view all Patients.")
permit (
    principal is Employee,
    action == Action::"ViewPatient",
    resource
)
when {
    principal.roles.contains(EmployeeRole::"Doctor") &&
    principal has level &&
    (
        principal.level == ProfessionalLevel::"Senior" ||
        principal.level == ProfessionalLevel::"Specialist"
    ) &&
    (
        resource is Patient ||
        resource is Visit
    )
}
unless {
    resource has confidentiality &&
    resource.confidentiality == Confidentiality::"Protected"
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
        principal is Employee &&
        principal.roles.contains(EmployeeRole::"Administrator")
    ) ||
    (
        action == Action::"ViewEmployee" &&
        resource.roles.containsAny([EmployeeRole::"Administrative Assistant",
                                    EmployeeRole::"Doctor"])
    )
};

// 'policy14'
// Every Employee can view the Clinics they are assigned to.
@id("Policy: Every Employee can view the Clinics they are assigned to.")
permit (
    principal is Employee,
    action == Action::"ViewClinic",
    resource is Clinic
)
when {
    principal.clinics.contains(resource)
};
