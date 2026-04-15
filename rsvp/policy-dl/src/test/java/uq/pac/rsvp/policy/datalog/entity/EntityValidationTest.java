package uq.pac.rsvp.policy.datalog.entity;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    void testFile(String entityFile) throws IOException, IllegalAccessException {
        Path path = Path.of(RESOURCES.toString(), entityFile + ".json");
        EntitySet entities = EntitySet.parse(path);

        String message = null;
        String location = null;
        String name = null;

        for (String s : Files.readAllLines(path).stream().filter(s -> s.startsWith("//")).toList()) {
            String trim = s.substring(s.indexOf(':') + 1).trim();
            if (s.startsWith("// @MESSAGE:")) {
                message = trim;
            } else if (s.startsWith("// @LOCATION:")) {
                location = trim;
            } else if (s.startsWith("// @NAME: ")) {
                name = trim;
            }
        }

        assertNotNull(message, "Missing message oracle");
        assertNotNull(location, "Missing location oracle");
        assertNotNull(name, "Missing name oracle");

        try {
            EntityValidator.validate(SCHEMA, entities);
        } catch (EntityValidator.Error error) {
            assertTrue(error.getMessage().startsWith(message),
                    "Mismatched message: \nexpected: '%s',\nactual:     '%s'".formatted(message, error.getMessage()));
        }
    }

    @Test
    void test() throws RsvpException, IOException, IllegalAccessException {
        testFile("duplicate");
    }
}
