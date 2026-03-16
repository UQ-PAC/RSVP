permit (
    principal,
    action == Foo::Bar::Action::"read",
    resource
) when {
    principal == Foo::Bar::Baz::"X"
};
