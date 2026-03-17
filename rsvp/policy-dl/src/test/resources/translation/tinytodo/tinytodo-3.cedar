// Policy 2: A User can see a List if they are either a reader or editor
 permit (
     principal,
     action == Action::"GetList",
     resource
 )
 when { principal in resource.readers || principal in resource.editors };
