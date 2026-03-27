package uq.pac.rsvp.policy.datalog.invariant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    Logger logger = new Logger();

    private static final String INPUT = """
        @invariant("foo0")
        true;
        @invariant("foo1")
        false;
        @invariant("foo2")
        false && false || !true;
        @invariant("foo3")
        principal;
        @invariant("foo4")
        principal.album.photo;
        @invariant("foo5")
        Principal::Album::Photo;
        @invariant("foo6")
        Resource::Picture::Kind::"Forest";
        @invariant("foo7")
        principal.album.photo && resource;
        @invariant("foo8")
        principal.album.photo || resource;
        @invariant("foo9")
        (principal.album.photo || resource);
        @invariant("foo10")
        !principal.album.photo;
        @invariant("foo11")
        !(principal.album.photo || resource);
        @invariant("foo12")
        a == b && c != d;
        @invariant("foo13")
        a == "a" && c != "c";
        @invariant("foo14")
        a == 1 && c != 2;
        @invariant("foo15")
        principal.friends.isEmpty();
        @invariant("foo16")
        principal.friends.contains(resource);
        @invariant("foo17")
        principal.friends.contains(resource, action);
        @invariant("foo18")
        allowedRequests(principal, resource, action);
        @invariant("foo19")
        resource has foo || principal has bar;
        @invariant("foo20")
        resource is Resource::Picture::Kind;
        @invariant("foo21")
        resource in Resource::Picture::Kind::"Forest";
        @invariant("foo22")
        resource == Resource::Picture::Kind::"Forest"
            for some resource: Resource::Picture::Kind;
        @invariant("foo23")
        resource.foo == "foo" && principal.bar == "bar"
            for all resource:  Resource::Picture, principal: Album::Photo;
        """;

    private static final String EXPECTED = """
        @invariant("foo0")
        true;
        @invariant("foo1")
        false;
        @invariant("foo2")
        ((false && false) || !true);
        @invariant("foo3")
        principal;
        @invariant("foo4")
        principal.album.photo;
        @invariant("foo5")
        Principal::Album::Photo;
        @invariant("foo6")
        Resource::Picture::Kind::"Forest";
        @invariant("foo7")
        (principal.album.photo && resource);
        @invariant("foo8")
        (principal.album.photo || resource);
        @invariant("foo9")
        (principal.album.photo || resource);
        @invariant("foo10")
        !principal.album.photo;
        @invariant("foo11")
        !(principal.album.photo || resource);
        @invariant("foo12")
        ((a == b) && (c != d));
        @invariant("foo13")
        ((a == "a") && (c != "c"));
        @invariant("foo14")
        ((a == 1) && (c != 2));
        @invariant("foo15")
        principal.friends.isEmpty();
        @invariant("foo16")
        principal.friends.contains(resource);
        @invariant("foo17")
        principal.friends.contains(resource, action);
        @invariant("foo18")
        allowedRequests(principal, resource, action);
        @invariant("foo19")
        ((resource has "foo") || (principal has "bar"));
        @invariant("foo20")
        (resource is Resource::Picture::Kind);
        @invariant("foo21")
        (resource in Resource::Picture::Kind::"Forest");
        @invariant("foo22")
        (resource == Resource::Picture::Kind::"Forest")
            for some resource: Resource::Picture::Kind;
        @invariant("foo23")
        ((resource.foo == "foo") && (principal.bar == "bar"))
            for all principal: Album::Photo, resource: Resource::Picture;
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

}
