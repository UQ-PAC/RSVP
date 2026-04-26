package uq.pac.rsvp.policy.ast.schema;

import com.cedarpolicy.model.exception.InternalException;
import com.cedarpolicy.model.schema.Schema;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.policy.ast.CedarschemaBaseVisitor;
import uq.pac.rsvp.policy.ast.CedarschemaLexer;
import uq.pac.rsvp.policy.ast.CedarschemaParser;

import static org.junit.jupiter.api.Assertions.fail;

public class SchemaParseTest {

    /**
     * Basic testing for Cedarschema grammar of cedar schemas
     * <p>
     * The following is a differential test checking whether:
     * (1) a string accepted by Cedar is also accepted by RSVP (text not prefixed)
     * (2) a string rejected by Cedar is also rejected by RSVP (input prefixed by '-')
     * (3) a string accepted by Cedar is rejected by RSVP (input prefixed by '~').
     * The latter is for the cases of something RSVP does not yet support.
     * For the moment this applies to tags only
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "namespace A {}", // Empty namespace is ok
            "-namespace {}",  // Namespaces require names

            "entity A;",     // One entity
            "entity A, B;",  // Multiple entities
            "namespace N { entity A; }", // One entity in namespace
            "namespace N { entity A, B; }", // Multiple entities in namespace
            "-entity \"A\";", // Entity names cannot be quoted
            "-entity A::B",   // Or be composite

            "entity Foo {};",  // '=' is optional
            "entity Foo = { };",
            "entity Foo   { name: String };",
            "entity Foo = { name: String };",
            "entity Foo = { name?: String };",      // optional attribute
            "entity Foo   { \"name\": String };",   // attributes can be quoted
            "entity Foo   { \"name\"?: String };",  // attributes can be quoted
            "entity Foo   { name: String, permission: { read: Bool, write: Bool } };",
            "entity Foo   { name: String, permission: { read: Set<Bool>, write: Set<Bool> } };",
            "-entity Foo  { foo::name: String };", // unquoted attributes are identifiers
            "-entity Foo  { foo\"name\": String };", // unquoted attributes are identifiers
            "-entity Foo  { foo::\"name\": String };", // unquoted attributes are identifiers

            "entity A; entity B in A;",
            "-entity A; entity B in \"A\";", // Must be entity type in the IN clause
            "entity A, B, C; entity Foo in [];", // Empty clause is ok
            "entity A, B, C; entity Foo in [A];",
            "entity A, B, C; entity Foo in [A, B];",
            "entity A enum [ \"A\" ];",
            "entity A, B enum [ \"A\" ];",
            "entity A, B enum [ \"A\", \"B\" ];",
            "-entity A, B enum [  ];", // enums cannot be empty
            "namespace A { entity B; }",
            "namespace A { entity B; } namespace C { entity D; }",
            "namespace A { entity B; } namespace C { entity D in A::B; }",
            // In expects entity types (ID or Path)
            "-namespace A { entity B; } namespace C { entity D in A::\"B\"; }",

            // Actions
            // Action names
            "action A;",   // Action names: be identifiers or strings
            "action A, B;",
            "action \"A\";",
            "action A, \"B\";",
            "namespace A { action B; }",
            "namespace A { action B; } namespace C { action D; }",
            "-namespace A { action A::B; };", // Action names cannot have '::'

            // in
            "action A; action B in A;",
            "namespace C { action A; action B in A; }", // can be ID
            "namespace C { action A; action B in \"A\"; }", // can be STRING
            "namespace C { action A; action B in Action::\"A\"; }", // can be quoted entity reference
            "-namespace C { action A; action B in Action::A; }",
            "namespace A { action B; } namespace D { action E in A::Action::\"B\"; }",
            "namespace A { action B; } namespace D { action E in [A::Action::\"B\"]; }",
            "namespace A { action B; } namespace D { action F; action E in [F, A::Action::\"B\"]; }",
            "action B; namespace C { action D in B; }",
            "-action B; namespace C { action D in []; }", // actions within IN cannot be empty
            "-namespace C { action A; action B in Action::A; }",
            "-namespace A { action B; } namespace C { action D in A::Action::B; }", // Quotes needed

            // Multiple references, same, but empty not accepted
            "action A, B; action C in [A, B];",
            "action A, B; action C in [A];",
            "-action A, B; action C in [];",
            "action A, B; action C in [\"A\", B];",
            "action A, B; action C in [\"A\"];",

            // applies to
            "-entity A, B, C; action D appliesTo { };", // unexpected token
            "-entity A, B, C; action D appliesTo { principal: A };", // missing resource
            "entity A, B, C; action D appliesTo { principal: A, resource: B };",
            "-entity A, B, C; action D appliesTo { principal: A, resource: B, context: Bool };", // type other than record
            "entity A, B, C; action D appliesTo { principal: A, resource: B, context: { auth: Bool } };",
            "-entity A, B, C; action D appliesTo { context: { auth: Bool } };", // missing resource
            "-entity A, B, C; action D appliesTo { resource: D, context: { auth: Bool } };", // missing principal

            // Common types
            "type T = String;",
            "type T = __cedar::String;",
            "type T = Set<String>;",
            "type T = { s: String };",
            "type T = { \"s\": String };",

            // Common type names cannot use built-in types
            "-type Bool = Bool;",
            "-type Boolean = Bool;",
            "-type Long = Bool;",
            "-type String = Bool;",
            "-type Set = Bool;",
            "-type Record = Bool;",
            "-type Entity = Bool;",
            "-type Extension = Bool;",

            // In addition to what Cedar insists on, RSVP adds built-in extension types
            // to avoid shadowing issues
            "~type Action = Bool;",
            "~type datetime = Bool;",
            "~type decimal = Bool;",
            "~type duration = Bool;",
            "~type ipaddr = Bool;",

            // Annotations: any component can be annotated
            "@annotation namespace A {}",
            "@a @b(\"c\") namespace A {}",
            "@annotation entity A;",
            "@a @b(\"c\") entity A;",
            "@annotation action A;",
            "@a @b(\"c\") action A;",
            "@annotation type D = Bool;",
            "@a @b(\"c\") type D = Bool;",

            // Tags feature of entities is unsupported by RSVP
            "~entity A { name: String } tags String;",
            "~entity A { name: String } tags { age: Long, surname: String };",

            // Trailing comma in record
            "entity A { name: String, };",
            "entity A { name: String, age: Long, };",
            "-entity A { , };",

            // Using keywords as attribute names
            "entity A { String: String };",
            "entity A { type: String };",
            "entity A { entity: String };",
            "entity A { action: String };",
            "entity A { principal: String };",
            "entity A { resource: String };",
            "entity A { appliesTo: String };",
            "entity A { enum: String };",
            "-entity A { in: String };",

    })
    void test(String text) throws InternalException {
        boolean cedarPositive = true;
        boolean rsvpPositive = true;
        if (text.startsWith("-")) {
            text = text.substring(1);
            cedarPositive = false;
            rsvpPositive = false;
        } else if (text.startsWith("~")) {
            text = text.substring(1);
            rsvpPositive = false;
        }

        // cedar
        try {
            Schema.parse(Schema.JsonOrCedar.Cedar, text);
            if (!cedarPositive) {
                fail("Unexpected success [cedar]: " + text);
            }
        } catch (InternalException e) {
            if (cedarPositive) {
                throw e;
            }
        }

        // rsvp
        try {
            parse(text);
            if (!rsvpPositive) {
                fail("Unexpected success [rsvp]: " + text);
            }
        } catch (ParseCancellationException c) {
            if (rsvpPositive) {
                throw c;
            }
        }
    }

    static String parse(String text) {
        SchemaParser.ThrowingErrorListener errorListener = new SchemaParser.ThrowingErrorListener();

        CedarschemaLexer lexer = new CedarschemaLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CedarschemaParser parser = new CedarschemaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return new CedarschemaBaseVisitor<String>() {
            @Override
            public String visitSchema(CedarschemaParser.SchemaContext ctx) {
                return ctx.getText();
            }
        }.visit(parser.schema());
    }

}
