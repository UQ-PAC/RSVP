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
            "namespace A::B {}", // Schema names can be compound
            "-namespace {}",  // but cannot be empty
            "-namespace B { namespace B {} }", // Namespaces cannot be nested
            "namespace B { } namespace A {}", // Multiple namespaces are ok
            "entity A; namespace B { } entity B;", // Global namespace is special
            "entity A; namespace B { entity C; } namespace E { entity D; }",
            // Definition of the same schema twice is not ok, but this is a semantic issue:
            // Cedar fails, RSVP does not (at this stage)
            "`entity A; namespace B { entity C; } namespace B { entity D; }",

            // Names of entities, types and actions cannot be compound
            "-entity A::B;",
            "-action A::B;",
            "-type A::B = String;",
            // Actions names can include arbitrary characters as long as the names are quoted
            "action \"A::B\";",
            // Entities and common types cannot be quoted
            "-entity \"A::B\";",
            "-type \"A::B\" = String",

            "entity A;",     // One entity
            "entity A, B;",  // Multiple entities
            "namespace N { entity A; }", // One entity in namespace
            "namespace N { entity A, B; }", // Multiple entities in namespace

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

            // References can be
            "namespace A { action B; action C in B; }", // unquoted identifiers (as action names)
            "namespace A { action B; action C in \"B\"; }",  // quoted identifiers (as action names)
            "namespace A { action B; action C in Action::\"B\"; }", // Relative action names, e.g. Action::"foo" in some namespace
            "namespace A { action B; action C in A::Action::\"B\"; }", // Absolute action names, e.g. NS:Action::"foo" in some namespace
            // References cannot be in the form of A::B (both unquoted)
            "-namespace A { action B; action C in A::B; }",

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
        // '-' both fail
        if (text.startsWith("-")) {
            text = text.substring(1);
            cedarPositive = false;
            rsvpPositive = false;
        // RSVP fails, Cedar does not
        } else if (text.startsWith("~")) {
            text = text.substring(1);
            rsvpPositive = false;
        // Cedar fails, RSVP does not
        } else if (text.startsWith("`")) {
            text = text.substring(1);
            cedarPositive = false;
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
