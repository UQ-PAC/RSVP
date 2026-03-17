package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.exception.AuthException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntitiesTest {

    private final static Path TESTDIR = Path.of(TestUtil.RESOURCEDIR.toString(), "entity");

    @ParameterizedTest
    @ValueSource(strings = {
            "entities-tab",
            "entities-undefined"
    })
    void unsupportedName(String name) throws IOException, AuthException, RsvpException  {
        Path entity = TestUtil.findFile(TESTDIR, name + ".json");
        Path schema = TestUtil.findFile(TESTDIR, ".cedarschema");
        Path policy = TestUtil.findFile(TESTDIR, ".cedar");
        assertThrows(TranslationError.class, () -> Translation.validate(schema, policy, entity));
    }

    @ParameterizedTest
    @ValueSource(strings = { "entities" })
    void supported(String name) throws IOException, AuthException, RsvpException  {
        Path entity = TestUtil.findFile(TESTDIR, name + ".json");
        Path schema = TestUtil.findFile(TESTDIR, ".cedarschema");
        Path policy = TestUtil.findFile(TESTDIR, ".cedar");
        assertDoesNotThrow(() -> Translation.validate(schema, policy, entity));
    }
}
