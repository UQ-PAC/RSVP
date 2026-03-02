// === View
//   + Anyone can view any entry from any database.
permit (
    principal is PetClinic::Visitor,
    action == PetClinic::Action::"ReadFromDatabase",
    resource is PetClinic::Database
);

//   + Anyone can view any web page.
permit (
    principal is PetClinic::Visitor,
    action == PetClinic::Action::"ViewWebPage",
    resource is PetClinic::WebPage
);

//  + Anyone can perform the "FindOwner" action.
permit (
    principal is PetClinic::Visitor,
    action == PetClinic::Action::"FindOwner",
    resource is PetClinic::WebPage
);

// === Add or Edit
//   + Anyone can add to or edit any database.
permit (
    principal is PetClinic::Visitor,
    action == PetClinic::Action::"WriteToDatabase",
    resource is PetClinic::Database
);

//   + Anyone can perform the actions: "AddOwner", "AddPet", "AddVisit",
//                                      "EditOwner", and "EditPet".
permit (
    principal is PetClinic::Visitor,
    action in [PetClinic::Action::"AddOwner", PetClinic::Action::"AddPet",
                PetClinic::Action::"AddVisit", PetClinic::Action::"EditOwner",
                PetClinic::Action::"EditPet"],
    resource is PetClinic::WebPage
);
