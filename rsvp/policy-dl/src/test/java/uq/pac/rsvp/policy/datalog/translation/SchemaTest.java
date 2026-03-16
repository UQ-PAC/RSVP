package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.exception.AuthException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaTest {

    private final static Logger logger = new Logger();

    private final static Path TESTDIR = Path.of(TestUtil.RESOURCEDIR.toString(), "schema");

    public static Path pathOf(String file) {
        return Path.of(TESTDIR.toString(), file);
    }

    @Test
    void unsupportedName() throws RsvpException, AuthException, IOException {
        List<Path> schemas = TestUtil.findFiles(TESTDIR, ".cedarschema");
        Path policy = TestUtil.findFile(TESTDIR, ".cedar");
        Path entities = TestUtil.findFile(TESTDIR, ".json");

        for (Path schema : schemas) {
            try {
                Translation.validate(schema, policy, entities);
            } catch (TranslationError e) {
                assertTrue(e.getMessage().startsWith("Unsupported action name:"));
            }
        }
    }
}
