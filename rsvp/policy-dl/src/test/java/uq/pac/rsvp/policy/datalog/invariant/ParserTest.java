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
        @invariant("foo")
        true;
        @invariant("foo")
        false;
        @invariant("foo")
        false && false || !true;
        @invariant("foo")
        principal;
        @invariant("foo")
        principal.album.photo;
        @invariant("foo")
        Principal::Album::Photo;
        @invariant("foo")
        Resource::Picture::Kind::"Forest";
        @invariant("foo")
        principal.album.photo && resource;
        @invariant("foo")
        principal.album.photo || resource;
        @invariant("foo")
        (principal.album.photo || resource);
        @invariant("foo")
        !principal.album.photo;
        @invariant("foo")
        !(principal.album.photo || resource);
        @invariant("foo")
        a == b && c != d;
        @invariant("foo")
        a == "a" && c != "c";
        @invariant("foo")
        a == 1 && c != 2;
        @invariant("foo")
        principal.friends.isEmpty();
        @invariant("foo")
        principal.friends.contains(resource);
        @invariant("foo")
        principal.friends.contains(resource, action);
        @invariant("foo")
        allowedRequests(principal, resource, action);
        @invariant("foo")
        resource has foo || principal has bar;
        @invariant("foo")
        resource is Resource::Picture::Kind;
        @invariant("foo")
        resource in Resource::Picture::Kind::"Forest";
        @invariant("foo")
        resource == Resource::Picture::Kind::"Forest"
            for some resource: Resource::Picture::Kind;
        @invariant("foo")
        resource.foo == "foo" && principal.bar == "bar"
            for all resource:  Resource::Picture, principal: Album::Photo;
        """;

    private static final String EXPECTED = """
        @invariant("foo")
        true;
        @invariant("foo")
        false;
        @invariant("foo")
        ((false && false) || !true);
        @invariant("foo")
        principal;
        @invariant("foo")
        principal.album.photo;
        @invariant("foo")
        Principal::Album::Photo;
        @invariant("foo")
        Resource::Picture::Kind::"Forest";
        @invariant("foo")
        (principal.album.photo && resource);
        @invariant("foo")
        (principal.album.photo || resource);
        @invariant("foo")
        (principal.album.photo || resource);
        @invariant("foo")
        !principal.album.photo;
        @invariant("foo")
        !(principal.album.photo || resource);
        @invariant("foo")
        ((a == b) && (c != d));
        @invariant("foo")
        ((a == "a") && (c != "c"));
        @invariant("foo")
        ((a == 1) && (c != 2));
        @invariant("foo")
        principal.friends.isEmpty();
        @invariant("foo")
        principal.friends.contains(resource);
        @invariant("foo")
        principal.friends.contains(resource, action);
        @invariant("foo")
        allowedRequests(principal, resource, action);
        @invariant("foo")
        ((resource has "foo") || (principal has "bar"));
        @invariant("foo")
        (resource is Resource::Picture::Kind);
        @invariant("foo")
        (resource in Resource::Picture::Kind::"Forest");
        @invariant("foo")
        (resource == Resource::Picture::Kind::"Forest")
            for some resource: Resource::Picture::Kind;
        @invariant("foo")
        ((resource.foo == "foo") && (principal.bar == "bar"))
            for all principal: Album::Photo, resource: Resource::Picture;
        """;

    @Test
    @DisplayName("Invariant Parsing")
    void parseTest() {
        List<Invariant> invariants = InvariantDriver.parse(INPUT);
        invariants.forEach(i -> logger.info(YELLOW, i + ";"));
         String text = invariants.stream()
                        .map(Invariant::toString)
                        .collect(Collectors.joining(";\n"));
         assertEquals(EXPECTED.trim(), text + ";");
    }

}
