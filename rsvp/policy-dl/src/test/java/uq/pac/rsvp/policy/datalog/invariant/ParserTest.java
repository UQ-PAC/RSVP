package uq.pac.rsvp.policy.datalog.invariant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

   private static final String INPUT = """
        for all true;
        for all false;
        for all false && false || !true;
        for all principal;
        for some principal.album.photo;
        for some Principal::Album::Photo;
        for some Resource::Picture::Kind::"Forest";
        for some principal.album.photo && resource;
        for some principal.album.photo || resource;
        for some (principal.album.photo || resource);
        for some !principal.album.photo;
        for some !(principal.album.photo || resource);
        for some a == b && c != d;
        for some a == "a" && c != "c";
        for some a == 1 && c != 2;
        for some resource has foo || principal has bar;
        for some resource is Resource::Picture::Kind;
        for some resource in Resource::Picture::Kind::"Forest";
        for some resource == Resource::Picture::Kind::"Forest" where resource is Resource::Picture::Kind;
        for some resource.foo == "foo" && principal.bar == "bar" where resource is Resource::Picture, principal is Album::Photo;
        """;

    private static final String EXPECTED = """
        for all true;
        for all false;
        for all ((false && false) || !true);
        for all principal;
        for some principal.album.photo;
        for some Principal::Album::Photo;
        for some Resource::Picture::Kind::"Forest";
        for some (principal.album.photo && resource);
        for some (principal.album.photo || resource);
        for some (principal.album.photo || resource);
        for some !principal.album.photo;
        for some !(principal.album.photo || resource);
        for some ((a == b) && (c != d));
        for some ((a == "a") && (c != "c"));
        for some ((a == 1) && (c != 2));
        for some ((resource has "foo") || (principal has "bar"));
        for some (resource is Resource::Picture::Kind);
        for some (resource in Resource::Picture::Kind::"Forest");
        for some (resource == Resource::Picture::Kind::"Forest") where resource is Resource::Picture::Kind;
        for some ((resource.foo == "foo") && (principal.bar == "bar")) where principal is Album::Photo, resource is Resource::Picture;
        """;

    @Test
    @DisplayName("Invariant Parsing")
    void parseTest() {
        String text = InvariantDriver.parse(INPUT).stream()
                .map(Invariant::toString)
                .collect(Collectors.joining(";\n"));
        assertEquals(EXPECTED.trim(), text + ";");
    }

}
