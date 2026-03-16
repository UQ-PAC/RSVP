package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.exception.AuthException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SchemaTest {

    private final static Path TESTDIR = Path.of(TestUtil.RESOURCEDIR.toString(), "schema");

    @Test
    void unsupportedName() throws RsvpException, AuthException, IOException {
        List<Path> schemas = TestUtil.findFiles(TESTDIR, ".cedarschema");
        Path policy = TestUtil.findFile(TESTDIR, ".cedar");
        Path entities = TestUtil.findFile(TESTDIR, ".json");

        for (Path schema : schemas) {
            try {
                Translation.validate(schema, policy, entities);
                fail();
            } catch (TranslationError e) {
                assertTrue(e.getMessage().matches("^Unsupported (action|entity) name:.*"));
            }
        }
    }
}
