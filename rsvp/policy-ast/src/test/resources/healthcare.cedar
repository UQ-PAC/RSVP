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
