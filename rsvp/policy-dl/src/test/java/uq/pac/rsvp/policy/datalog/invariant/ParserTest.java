package uq.pac.rsvp.policy.datalog.invariant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.StdLogger;

import java.util.Arrays;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    StdLogger logger = new StdLogger();

    private static final String INPUT = """
        // Literal
        true
        false
        false && false || !true
        // Variable
        principal
        // Property access
        principal.album.photo
        // Property access with function call
        principal.album.photo.isEmpty()
        // Function call without property access
        allow(principal, resource, action)
        // Type
        Principal::Album::Photo
        // Entity
        Resource::Picture::Kind::"Forest"
        Photoapp::Action::"view"
        // Literal string
        "view"
        // Literal long
        42
        -42
        // Literal Array
        []
        [1]
        [1, 2]
        // In
        resource is Resource::Picture::Kind
        // Is
        resource in Resource::Picture::Kind::"Forest"
        // Has
        principal has "attr"
        principal has attr;
        // Conjunction
        principal.album.photo && resource
        // Disjunction
        principal.album.photo || resource
        (principal.album.photo || resource)
        // Negation
        !principal.album.photo
        !(principal.album.photo || resource)
        // Equality
        a == b && c != d
        a == "a" && c != "c"
        a == 1 && c != 2
        // Quantifiers
        resource == Resource::Picture::Kind::"Forest" for some resource: Resource::Picture::Kind;
        resource.foo == "foo" && principal.bar == "bar" for all resource:  Resource::Picture, principal: Album::Photo;
        // Arithmetic precedence
        -1 + 2 * 3 == 6 * 7 + 8
        // Conditional expressions
        if true then false else true
        "a\\nb\\t\\""
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
        invariant [];
        invariant [1];
        invariant [1, 2];
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
        List<String> strings = Arrays.stream(INPUT.split("\\n"))
                .filter(l -> !l.trim().startsWith("//"))
                .toList();

        StringBuilder sb = new StringBuilder();
        strings.forEach(s -> {
            String text = "invariant %s;".formatted(s);
            logger.info(YELLOW, text);
            Invariant invariant = PolicyProgram.parse(text).invariants().findFirst().orElseThrow();
            sb.append(invariant).append('\n');
        });

        assertEquals(EXPECTED.trim(), sb.toString().trim());
    }

    // Some components of the invariant checking rely on the fact that boolean type
    // object are only equal if they are the same object.
    @Test
    void equals() {
        assertNotEquals(new BooleanType(), new BooleanType());
    }
}
