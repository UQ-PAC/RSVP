permit (
    principal,
    action == Action::"read",
    resource
)
when { resource in principal.documentsAndFoldersWithViewAccess };

