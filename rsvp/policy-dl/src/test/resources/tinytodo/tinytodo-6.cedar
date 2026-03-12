// Policy 5: Interns may not create new task lists
forbid (
    principal in Team::"interns",
    action == Action::"CreateList",
    resource == Application::"TinyTodo"
);
