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
        invariant true;
        invariant false;
        invariant false && false || !true;
        // Variable
        invariant principal;
        // Property access
        invariant principal.album.photo;
        // Property access with function call
        invariant principal.album.photo.isEmpty();
        // Function call without property access
        invariant allow(principal, resource, action);
        // Type
        invariant Principal::Album::Photo;
        // Entity
        invariant Resource::Picture::Kind::"Forest";
        invariant Photoapp::Action::"view";
        // Literal string
        invariant "view";
        // Literal long
        invariant 42;
        invariant -42;
        // In
        invariant resource is Resource::Picture::Kind;
        // Is
        invariant resource in Resource::Picture::Kind::"Forest";
        // Has
        invariant principal has "attr";
        invariant principal has attr;
        // Conjunction
        invariant principal.album.photo && resource;
        // Disjunction
        invariant principal.album.photo || resource;
        invariant (principal.album.photo || resource);
        // Negation
        invariant !principal.album.photo;
        invariant !(principal.album.photo || resource);
        // Equality
        invariant a == b && c != d;
        invariant a == "a" && c != "c";
        invariant a == 1 && c != 2;
        // Quantifiers
        invariant resource == Resource::Picture::Kind::"Forest" for some resource: Resource::Picture::Kind;
        invariant resource.foo == "foo" && principal.bar == "bar" for all resource:  Resource::Picture, principal: Album::Photo;
        // Arithmetic precedence
        invariant -1 + 2 * 3 == 6 * 7 + 8;
        // Conditional expressions
        invariant if true then false else true;
        invariant "a\\nb\\t\\"";
        """;

    private static final String EXPECTED = """
        invariant true;
        invariant false;
        invariant ((false && false) || !true);
        invariant principal;
        invariant principal.album.photo;
        invariant principal.album.photo.isEmpty();
        invariant allow(principal, resource, action);
        invariant Principal::Album::Photo;
        invariant Resource::Picture::Kind::"Forest";
        invariant Photoapp::Action::"view";
        invariant "view";
        invariant 42;
        invariant -42;
        invariant (resource is Resource::Picture::Kind);
        invariant (resource in Resource::Picture::Kind::"Forest");
        invariant (principal has "attr");
        invariant (principal has "attr");
        invariant (principal.album.photo && resource);
        invariant (principal.album.photo || resource);
        invariant (principal.album.photo || resource);
        invariant !principal.album.photo;
        invariant !(principal.album.photo || resource);
        invariant ((a == b) && (c != d));
        invariant ((a == "a") && (c != "c"));
        invariant ((a == 1) && (c != 2));
        invariant (resource == Resource::Picture::Kind::"Forest")
            for some resource: Resource::Picture::Kind;
        invariant ((resource.foo == "foo") && (principal.bar == "bar"))
            for all resource: Resource::Picture, principal: Album::Photo;
        invariant ((-1 + (2 * 3)) == ((6 * 7) + 8));
        invariant (if true; then false; else true);
        invariant "a\\nb\\t\\"";
        """;

    @Test
    @DisplayName("Invariant Parsing")
    void parseTest() {
        uq.pac.rsvp.policy.ast.invariant.InvariantSet invariants = uq.pac.rsvp.policy.ast.invariant.InvariantSet.parse(INPUT);
        invariants.stream().forEach(i -> logger.info(YELLOW, i + ""));
        String text = invariants.stream()
                        .map(uq.pac.rsvp.policy.ast.invariant.Invariant::toString)
                        .collect(Collectors.joining("\n"));
        assertEquals(EXPECTED.trim(), text);
    }

    // Some components of the invariant checking rely on the fact that boolean type
    // object are only equal if they are the same object.
    @Test
    void equals() {
        assertNotEquals(new BooleanType(), new BooleanType());
    }
}
