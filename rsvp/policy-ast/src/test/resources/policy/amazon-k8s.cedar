// Users can edit their own info, admins can edit anyone's info
permit (
    principal,
    action,
    resource in HealthCareApp::InfoType::"accountinfo"
)
when
{ resource.patient == principal || principal in HealthCareApp::Role::"admin" };

//A patient may create an appointment for themselves, or an administrator can do it
permit (
    principal,
    action == HealthCareApp::Action::"createAppointment",
    resource
)
when
{
    (context.referrer in HealthCareApp::Role::"doctor" &&
     resource.patient == principal) ||
    principal in HealthCareApp::Role::"admin"
};

// Explicity deny policy to block all actions for non-executives.
forbid (principal, action, resource)
when
{
    resource has topSecret &&
    resource.topSecret == true &&
    !(principal in SecureDocs::Group::"Executive")
};

// Policy to allow executives to perform all actions
permit (
    principal in SecureDocs::Group::"Executive",
    action,
    resource
);

permit (
    principal,
    action == SecureDocs::Action::"read",
    resource
)
when
{
    principal has clearanceLevel &&
    principal.clearanceLevel >= resource.viewerLevel &&
    principal.dept == resource.dept
};

@annotation
permit (
    principal,
    action == SecureDocs::Action::"write",
    resource
)
when
{
    principal has clearanceLevel &&
    principal.clearanceLevel >= resource.editorLevel &&
    principal.clearanceLevel >= 2 &&
    principal.dept == resource.dept
};

// only a list owner can view, update or delete a task
@id("list_owner_can_view_update_delete_task")
permit (
    principal,
    action in
        [SimpleTodo::Action::"viewTask",
         SimpleTodo::Action::"updateTask",
         SimpleTodo::Action::"deleteTask"],
    resource
)
when { resource.belongs_to.owner == principal };

// only list owner can create a new task
@id("list_owner_can_create_task")
permit (
    principal,
    action in [SimpleTodo::Action::"createTask"],
    resource
)
when { resource.owner == principal };

// anyone can create a new list
@id("anyone_can_create_list")
permit (
    principal,
    action in [SimpleTodo::Action::"createList"],
    resource
)
when { true };

permit (
    principal == PhotoApp::User::"alice",
    action == PhotoApp::Action::"viewPhoto",
    resource == PhotoApp::Photo::"vacationPhoto.jpg"
);

permit (
    principal == PhotoApp::User::"stacey",
    action == PhotoApp::Action::"viewPhoto",
    resource
)
when { resource in PhotoApp::Account::"stacey" };

// developers can list configmaps
permit (
    principal in k8s::Group::"developers",
    action in [k8s::Action::"list", k8s::Action::"watch"],
    resource is k8s::Resource
)
when { resource.resource == "configmaps" && resource.apiGroup == "" };

// prevent users in self-configmaps-only from listing configmaps
// unless they specify a label selector of owner=principal.name
forbid (
    principal is k8s::User in k8s::Group::"self-configmaps-only",
    action in [k8s::Action::"list", k8s::Action::"watch"],
    resource is k8s::Resource
)
when { resource.resource == "configmaps" && resource.apiGroup == "" }
unless
{
    resource has labelSelector &&
    resource.labelSelector
        .contains
        (
            { "key": "owner", "operator": "=", "values": [principal.name] }
        )
};

// A Kubernetes service account can get and update the node status only for the node it runs on
permit (
    principal is k8s::ServiceAccount,
    action in [k8s::Action::"get", k8s::Action::"update", k8s::Action::"patch"],
    resource is k8s::Resource
)
when
{
    principal.name == "csi-driver" &&
    principal.namespace == "kube-system" &&
    resource.apiGroup == "" &&
    resource.resource == "nodes" &&
    resource has subresource &&
    resource.subresource == "status" &&
    principal has extra &&
    resource has name &&
    principal.extra
        .contains
        (
            {
                "key": "authentication.kubernetes.io/node-name",
                "values": [resource.name]
            }
        )
};

permit (
    principal in
        AWS::IdentityStore::Group::"90677fa0fb-5513f9a2-9916-4fc2-aec5-0358ce119215",
    action == AWS::SSM::Action::"getTokenForInstanceAccess",
    resource
)
when { resource.hasTag("Prod") && resource.getTag("Prod") == "Database" };

permit (
    principal,
    action == AWS::SSM::Action::"getTokenForInstanceAccess",
    resource
)
when
{
    context.iam.principalTags.hasTag("Role") &&
    context.iam.principalTags.getTag("Role") == "Admin"
};

permit (
    principal,
    action == AWS::SSM::Action::"getTokenForInstanceAccess",
    resource
)
when
{
    resource.hasTag("Environment") &&
    resource.getTag("Environment") == "Development"
};
