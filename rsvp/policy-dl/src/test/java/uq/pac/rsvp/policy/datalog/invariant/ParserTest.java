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
        true;
        false;
        false && false || !true;
        principal;
        principal.album.photo;
        Principal::Album::Photo;
        Resource::Picture::Kind::"Forest";
        principal.album.photo && resource;
        principal.album.photo || resource;
        (principal.album.photo || resource);
        !principal.album.photo;
        !(principal.album.photo || resource);
        a == b && c != d;
        a == "a" && c != "c";
        a == 1 && c != 2;
        principal.friends.isEmpty();
        principal.friends.contains(resource);
        principal.friends.contains(resource, action);
        allowedRequests(principal, resource, action);
        resource has foo || principal has bar;
        resource is Resource::Picture::Kind;
        resource in Resource::Picture::Kind::"Forest";
        resource == Resource::Picture::Kind::"Forest"
            for some resource: Resource::Picture::Kind;
        resource.foo == "foo" && principal.bar == "bar"
            for all resource:  Resource::Picture, principal: Album::Photo;
        """;

    private static final String EXPECTED = """
        true;
        false;
        ((false && false) || !true);
        principal;
        principal.album.photo;
        Principal::Album::Photo;
        Resource::Picture::Kind::"Forest";
        (principal.album.photo && resource);
        (principal.album.photo || resource);
        (principal.album.photo || resource);
        !principal.album.photo;
        !(principal.album.photo || resource);
        ((a == b) && (c != d));
        ((a == "a") && (c != "c"));
        ((a == 1) && (c != 2));
        principal.friends.isEmpty();
        principal.friends.contains(resource);
        principal.friends.contains(resource, action);
        allowedRequests(principal, resource, action);
        ((resource has "foo") || (principal has "bar"));
        (resource is Resource::Picture::Kind);
        (resource in Resource::Picture::Kind::"Forest");
        (resource == Resource::Picture::Kind::"Forest") for some resource: Resource::Picture::Kind;
        ((resource.foo == "foo") && (principal.bar == "bar")) for all principal: Album::Photo, resource: Resource::Picture;
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
