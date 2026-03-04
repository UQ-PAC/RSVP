// Every User can view any public page.
permit (
    principal is PetClinic::User,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
)
when {
    resource.public
};


