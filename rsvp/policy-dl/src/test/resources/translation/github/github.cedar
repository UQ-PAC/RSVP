// Users can perform an action if they have been granted the necessary permission.
// Policy 1
permit (
    principal,
    action == Action::"read",
    resource
)
when { principal in resource.readers };

// Policy 2
permit (
    principal,
    action in Action::"triage",
    resource
)
when { principal in resource.triagers };

// Policy 3
permit (
    principal,
    action in Action::"write",
    resource
)
when { principal in resource.writers };

// Policy 4
permit (
    principal,
    action in Action::"maintain",
    resource
)
when { principal in resource.maintainers };

// Policy 5
permit (
    principal,
    action in Action::"admin",
    resource
)
when { principal in resource.admins };

// Users also inherit permissions from the owner of a repository.
// Policy 6
permit (
    principal,
    action == Action::"read",
    resource
)
when { principal in resource.owner.readers };

// Policy 7
permit (
    principal,
    action in Action::"write",
    resource
)
when { principal in resource.owner.writers };

// Policy 8
permit (
    principal,
    action in Action::"admin",
    resource
)
when { principal in resource.owner.admins };
