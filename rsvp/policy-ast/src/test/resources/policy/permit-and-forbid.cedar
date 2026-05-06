permit (
    principal,
    action,
    resource
)
when { principal == "poppy" };

forbid (
    principal,
    action,
    resource
)
when { action == "murder" };