package uq.pac.rsvp.policy.datalog.entity;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.EntityException;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EntityValidationTest {

    private static final Path RESOURCES = TestUtil.getResourceDir("entity");
    private static final Schema SCHEMA;
    static {
        try {
            SCHEMA = Schema.parseCedarSchema(Path.of(RESOURCES.toString(), "entity.cedarschema"));
        } catch (RsvpException e) {
            throw new RuntimeException(e);
        }
    }

    void testFile(Path entityFile) throws IOException, IllegalAccessException {

        String expectedMessage = null;
        // We allow multiple locations for rare cases of non-deterministic element processing,
        // e.g., duplicate values
        Set<String> expectedLocations = new HashSet<>();

        for (String s : Files.readAllLines(entityFile).stream().filter(s -> s.startsWith("//")).toList()) {
            String trim = s.substring(s.indexOf(':') + 1).trim();
            if (s.startsWith("// @MESSAGE:")) {
                expectedMessage = trim;
            } else if (s.startsWith("// @LOCATION:")) {
                expectedLocations.add(trim);
            }
        }

        assertNotNull(expectedMessage, "Missing message oracle");
        assertFalse(expectedLocations.isEmpty(), "Missing location oracle");

        try {
            EntitySet entities = EntitySet.parse(entityFile);
            EntityValidator.validate(SCHEMA, entities);
            fail("expected EntityException thrown");
        } catch (EntityException error) {
            String [] parts = error.getMessage().split("\\r?\\n");
            // Error message of entity validator is in two parts (NL-separated)
            // * Error message
            // * Location of the form 'at file:offset:length [line:column-line-column]'

            assertEquals(2, parts.length);
            String actualMessage = parts[0].trim();
            assertEquals(expectedMessage, actualMessage);

            String actualLocation = parts[1].trim();

            expectedLocations = expectedLocations.stream()
                    .map(l -> "at %s:%s".formatted(entityFile.toString(), l))
                    .collect(Collectors.toSet());

            if (expectedLocations.size() == 1) {
                assertEquals(expectedLocations.stream().findAny().orElseThrow(), actualLocation);
            } else {
                assertTrue(expectedLocations.contains(actualLocation),
                        "Location " + actualLocation + " not found in \n" + expectedLocations);
            }
        }
    }

    @TestFactory
    Collection<DynamicTest> test() {
        return TestUtil.findFiles(RESOURCES, ".json").stream().map(p -> {
            String fn = p.getFileName().toString();
            String basename = fn.substring(0, fn.length() - 5);
            return DynamicTest.dynamicTest(basename, () -> testFile(p));
        }).toList();
    }

}
