// 'policy0'
// Every User can view any public page.
@id("Policy: Public page.")
permit (
    principal is PetClinic::User,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
)
when {
    resource.public
};

// 'policy1'
// No Guest User can view any actionable page.
@id("Policy: No Guest on actionable page.")
forbid (
    principal is PetClinic::User,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
)
when {
    principal in PetClinic::Guest::"Guest" &&
    resource.actionable
};

// 'policy2'
// No Guest User can view any private page.
@id("Policy: No Guest on private page.")
forbid (
    principal is PetClinic::User,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
)
when {
    principal in PetClinic::Guest::"Guest" &&
    !resource.public
};

// 'policy3'
// Every Administrator can do anything, anywhere.
@id("Policy: Administrator.")
permit (
    principal is PetClinic::User,
    action,
    resource
)
when {
    principal in PetClinic::Administrator::"Administrator"
};

// 'policy4'
// Every registered User (i.e. in a Users database) can view any private page.
// > > Too permissive! < <
@id("Policy: Registered User on private page.")
permit (
    principal is PetClinic::User,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
)
when {
    (
        principal in PetClinic::Database::"Clinic A" ||
        principal in PetClinic::Database::"Clinic B"
    ) &&
    !resource.public
};

// 'policy5'
// Every registered User (i.e. in a Users database) can view any actionable page.
// > > Too permissive! < <
@id("Policy: Registered User on actionable page.")
permit (
    principal is PetClinic::User,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
)
when {
    (
        principal in PetClinic::Database::"Clinic (A)" ||
        principal in PetClinic::Database::"Clinic (B)"
    ) &&
    resource.actionable
}
unless {
    context has databases &&
    !(principal in context.databases)
};

// 'policy6'
// Sharon Jenkins (Clinic A) cannot view data from Clinic B.
// >> She should be able to see George Franklin, but cannot! <<
@id("Policy: Sharon Jenkins on Clinic B.")
forbid (
    principal == PetClinic::User::"Sharon Jenkins",
    action == PetClinic::Action::"ViewWebPage",
    resource == PetClinic::WebPage::"Owner"
) when {
    context has databases &&
    context.databases.contains(PetClinic::Database::"Clinic B")
};
