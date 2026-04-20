package uq.pac.rsvp.policy.datalog.entity;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.support.SourceLoc;

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

    void assertLoc(String expected, EntityValue value) {
        assertLoc(expected, value.getLocation());
    }

    void assertLoc(String expected, SourceLoc actual) {
        assertEquals(actual.file + ":" + expected, actual.toString());
    }

    @Test
    void locationTest() throws IOException, IllegalAccessException {
        String json = """
        [{
            "uid": { "type": "Directory", "id": "/etc" },
            "attrs": {
                "partition": { "type": "Partition", "id": "sda" },
                "size" : 128,
                "path": "/etc",
                "access": "readonly",
                "permission": {
                    "read" : false,
                    "write": true,
                    "exec" : true
                },
                "dev": [ "sda", "sdb" ]
            },
            "parents": [
                { "type": "Partition", "id": "sda" }
            ]
        }]
        """;
        EntitySet entities = EntitySet.parse("entities.json", json);
        Entity entity = entities.getEntities().stream().findAny().orElseThrow();
        assertEquals(1, entities.getEntities().size());

        RecordValue attrs = entity.getAttrs();
        EntityReference reference = entity.getEuid();

        // Entity reference
        assertLoc("14:37 [2:12-2:48]", reference);

        // Attribute
        AttributeName partition = attrs.getAttributeName("partition");
        assertLoc("76:11 [4:9-4:19]", partition.getLocation());

        // Integer
        LongValue size = (LongValue) attrs.getValue("size");
        assertLoc("144:3 [5:18-5:20]", size);

        // Boolean
        RecordValue permission = (RecordValue) attrs.getValue("permission");
        BooleanValue read = (BooleanValue) permission.getValue("read");
        BooleanValue write = (BooleanValue) permission.getValue("write");

        assertLoc("248:5 [9:22-9:26]", read);
        assertLoc("276:4 [10:22-10:25]", write);

        // String
        StringValue path = (StringValue) attrs.getValue("path");
        assertLoc("165:6 [6:17-6:22]", path);

        // Record
        assertLoc("225:92 [8:23-12:9]", permission);

        // Set
        SetValue dev = (SetValue) attrs.getValue("dev");
        assertLoc("334:16 [13:16-13:31]", dev);

        // Entity
        assertLoc("1:426 [1:2-18:1]", entity.getLocation());
    }

}
