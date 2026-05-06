package uq.pac.rsvp.policy.ast.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;


public class PolicyParseTest {

    @ParameterizedTest
    @ValueSource(strings = {
            // Booleans
            "true",
            "false",
            // Long literals
            "1",
            "42",
            "003",
            "-42",
            // String literals
            "\"abc\"",
            "\"a\"",
            "\"\"",
            "\"foo\tbar\tbaz\"", // TODO: Look into string escaping more
            // Variables
            "principal",
            "resource",
            "action",
            // Entity Literal
            "Foo::\"bar\"",
            "Foo::Bar::\"baz\"",
            // Action literal
            "Action::\"view\"",
            "Foo::Action::\"view\"",
             // Set literal
            "[]",
            "[ ]",
            "[ 1 ]",
            "[ 1, 2 ]",
            "[ 1, 2, 3 ]",
            // Record Literal
            "{}",
            "{ a:1, b:2 }",
            "{ a:1, b:2, }",
            "{ \"a\":1, \"b\":2 }",
            "{ a:1, b:2, c:1 }",
            "{ a:1, b:2, c: [ 1, 2 ]  }",
            "{ a:1, b:2, c: {d : [ 1, 2 ] } }",
            // Extension functions
            "foo()",
            "bar::foo()",
            "baz::bar::foo()",
            "foo([], {}, true)",
            "bar::foo([], {}, true)",
            "baz::bar::foo([], {}, true)",
            // Property access
            "principal.context",
            "principal.context.read",
            "principal.context.read.allow",
            // Function application (limit to variables and properties)
            "principal.numbers.contains(4)",
            "principal.contains(4)",
            // Grouping
            "(1 + 1) * 3",
            // Arithmetic operators
            "1 + 1",
            "1 - 1",
            "1 * 1",
            // Comparison operators
            "principal.a == principal.b",
            "principal.a != principal.b",
            "principal.a > principal.b",
            "principal.a < principal.b",
            "principal.a >= principal.b",
            "principal.a <= principal.b",
            // has
            "principal.attr has foo",
            "principal.attr has \"foo\"",
            // is
            "principal is Account",
            "principal is User::Account",
            // in
            "principal in Account::\"Alice\"",
            "principal in User::Account::\"Alice\"",
            // logical
            "principal.a && principal.b",
            "principal.a || principal.b",
            "!principal.a",
            // Conditional
            "if true then false else true",
            // like
            "\"eggs\" like \"ham*\""
    })
    void testExpression(String text) {
        String input = "invariant " + text + ";";
        Invariant invariant = PolicyProgram.parse(input).invariants().findFirst().orElseThrow();
        System.out.println(invariant.getExpression());
    }

    // FIXME: More tests are needed

    @ParameterizedTest
    @ValueSource(strings = {
            "permit (principal, action, resource);",
            "permit (principal, action, resource) when { a };",
            "permit (principal, action, resource) unless { b };",
            "permit (principal, action, resource) when {a } unless { b };",
            "permit (principal, action, resource) unless { b } when { a };",
            "permit (principal, action, resource) unless { b } unless { a };",
            "permit (principal, action, resource) when { b } when { a };",
            "permit (principal, action, resource) when { b } when { a } unless { c };",
    })
    void testPolicyCondition(String text) {
        PolicyProgram.parse(text).policies().findFirst().orElseThrow();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "amazon-k8s.cedar",
        "annotation.cedar",
        "contains.cedar",
        "empty.cedar",
        "euid.cedar",
        "healthcare.cedar",
        "is.cedar",
        "permit-all.cedar",
        "permit-and-forbid.cedar",
        "policy-with-has.cedar"
    })
    void testParse(String file) throws IOException {
        URL url = ClassLoader.getSystemResource(file);
        PolicyProgram.parse(Path.of(url.getPath())).getPolicies();
    }
}
