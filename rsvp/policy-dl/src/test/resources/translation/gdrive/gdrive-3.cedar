permit (
    principal,
    action in [Action::"read", Action::"write", Action::"share"],
    resource
)
when
{ resource in principal.ownedDocuments || resource in principal.ownedFolders };
