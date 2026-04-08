package uq.pac.rsvp.policy.datalog.invariant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ParserTest {

    Logger logger = new Logger();

    private static final String INPUT = """
        // Literal
        @invariant("i0") true;
        @invariant("i1") false;
        @invariant("i2") false && false || !true;
        // Variable
        @invariant("i3") principal;
        // Property access
        @invariant("foo4") principal.album.photo;
        // Property access with function call
        @invariant("i5") principal.album.photo.isEmpty();
        // Function call without property access
        @invariant("i6") allow(principal, resource, action);
        // Type
        @invariant("i7") Principal::Album::Photo;
        // Entity
        @invariant("i8") Resource::Picture::Kind::"Forest";
        @invariant("i9") Photoapp::Action::"view";
        // Literal string
        @invariant("i10") "view";
        // Literal long
        @invariant("i11") 42;
        @invariant("i12") -42;
        // In
        @invariant("i13") resource is Resource::Picture::Kind;
        // Is
        @invariant("i14") resource in Resource::Picture::Kind::"Forest";
        // Has
        @invariant("i15") principal has "attr";
        @invariant("i16") principal has attr;
        // Conjunction
        @invariant("i17") principal.album.photo && resource;
        // Disjunction
        @invariant("i18") principal.album.photo || resource;
        @invariant("i19") (principal.album.photo || resource);
        // Implication
        @invariant("i20") principal.album.photo --> resource;
        // Negation
        @invariant("i21") !principal.album.photo;
        @invariant("i22") !(principal.album.photo || resource);
        // Equality
        @invariant("i23") a == b && c != d;
        @invariant("i24") a == "a" && c != "c";
        @invariant("i25") a == 1 && c != 2;
        // Quantifiers
        @invariant("i26") resource == Resource::Picture::Kind::"Forest" for some resource: Resource::Picture::Kind;
        @invariant("i27") resource.foo == "foo" && principal.bar == "bar" for all resource:  Resource::Picture, principal: Album::Photo;
        // Arithmetic precedence
        @invariant("i28") -1 + 2 * 3 == 6 * 7 + 8;
        // Conditional expressions
        @invariant("i29") if true then false else true;
        @invariant("i30") "a\\nb\\t\\"";
        """;

    private static final String EXPECTED = """
        @invariant("i0")
        true;
        @invariant("i1")
        false;
        @invariant("i2")
        ((false && false) || !true);
        @invariant("i3")
        principal;
        @invariant("foo4")
        principal.album.photo;
        @invariant("i5")
        principal.album.photo.isEmpty();
        @invariant("i6")
        allow(principal, resource, action);
        @invariant("i7")
        Principal::Album::Photo;
        @invariant("i8")
        Resource::Picture::Kind::"Forest";
        @invariant("i9")
        Photoapp::Action::"view";
        @invariant("i10")
        "view";
        @invariant("i11")
        42;
        @invariant("i12")
        -42;
        @invariant("i13")
        (resource is Resource::Picture::Kind);
        @invariant("i14")
        (resource in Resource::Picture::Kind::"Forest");
        @invariant("i15")
        (principal has "attr");
        @invariant("i16")
        (principal has "attr");
        @invariant("i17")
        (principal.album.photo && resource);
        @invariant("i18")
        (principal.album.photo || resource);
        @invariant("i19")
        (principal.album.photo || resource);
        @invariant("i20")
        (!principal.album.photo || resource);
        @invariant("i21")
        !principal.album.photo;
        @invariant("i22")
        !(principal.album.photo || resource);
        @invariant("i23")
        ((a == b) && (c != d));
        @invariant("i24")
        ((a == "a") && (c != "c"));
        @invariant("i25")
        ((a == 1) && (c != 2));
        @invariant("i26")
        (resource == Resource::Picture::Kind::"Forest")
            for some resource: Resource::Picture::Kind;
        @invariant("i27")
        ((resource.foo == "foo") && (principal.bar == "bar"))
            for all resource: Resource::Picture, principal: Album::Photo;
        @invariant("i28")
        ((-1 + (2 * 3)) == ((6 * 7) + 8));
        @invariant("i29")
        (if true; then false; else true);
        @invariant("i30") 
        "a\\nb\\t\\"";
        """;

    @Test
    @DisplayName("Invariant Parsing")
    void parseTest() {
        InvariantSet invariants = InvariantSet.parse(INPUT);
        invariants.stream().forEach(i -> logger.info(YELLOW, i + ";"));
        String text = invariants.stream()
                        .map(Invariant::toString)
                        .collect(Collectors.joining(";\n"));
        assertEquals(EXPECTED.trim(), text + ";");
    }

    // Some components of the invariant checking rely on the fact that boolean type
    // object are only equal if they are the same object.
    @Test
    void equals() {
        assertNotEquals(new BooleanType(), new BooleanType());
    }
}
