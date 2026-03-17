// Policy 3: A User can update a List and its tasks if they are an editor
 permit (
     principal,
     action in
         [Action::"UpdateList",
          Action::"CreateTask",
          Action::"UpdateTask",
          Action::"DeleteTask"],
     resource
 )
 when { principal in resource.editors };
