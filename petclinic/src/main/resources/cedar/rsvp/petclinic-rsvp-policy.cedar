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

// Every registered User (in a database) can view any private page.
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

// Sharon Jenkins (Clinic A) cannot view data from Clinic B.
@id("Policy: Sharon Jenkins on Clinic B.")
forbid (
    principal == PetClinic::User::"Sharon Jenkins",
    action == PetClinic::Action::"ViewWebPage",
    resource == PetClinic::WebPage::"Owner"
) when {
    context has database && context has readAction &&
    context.database in PetClinic::Database::"Clinic B" &&
    context.readAction == PetClinic::Action::"ReadFromDatabase"
};

// Every registered User (in a database) can view any actionable page.
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
};

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
