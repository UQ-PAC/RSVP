// Locations set in the statement visitor
permit ( principal, action, resource );

permit ( principal, action, resource ) when { true };

permit (
    principal is App::Account,
    action == Action::"view",
    resource is App::Photo
);

forbid (
    principal is App::Account in App::Account::"Alice",
    action == Action::"view",
    resource is App::Photo in App::Photo::"Meadow"
);

// Locations from the expression visitor
// Literals
permit (principal, action, resource)
when {
    true && false && 12 == "bar"
};

// Entity, Action, Var, Property
permit (principal, action, resource)
when {
    action == Action::"view" &&
    principal.album.photo.owner == Account::"Bob"
};

// Is, In, Has, Type
permit (principal, action, resource)
when {
    principal in Account::"Alice" &&
    principal is Account &&
    principal has attr
};

// Set
permit (principal, action, resource)
when {
    [1, 2, 3] == [2, 3, 1]
};

// Record
permit (principal, action, resource)
when {
    { "attr": 2 } != { "attr" : 3 }
};


// Conditional
permit (principal, action, resource)
when {
    if true then false else false
};

// Call
permit (principal, action, resource)
when {
    principal.album.contains(resource.photo)
};

// Unary
permit (principal, action, resource)
when {
    !false && -1 == 2
};

// Invariant
invariant
    principal.albums.isEmpty()
        for all principal: App::Account;