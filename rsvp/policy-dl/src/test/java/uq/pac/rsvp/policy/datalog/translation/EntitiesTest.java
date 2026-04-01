package uq.pac.rsvp.policy.datalog.translation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.policy.datalog.TestUtil;

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
    void unsupportedName(String name) {
        Path entity = TestUtil.findFile(TESTDIR, name + ".json"),
                schema = TestUtil.findFile(TESTDIR, ".cedarschema"),
                policy = TestUtil.findFile(TESTDIR, ".cedar"),
                invariants = TestUtil.findFile(TESTDIR, ".invariant");
        assertThrows(TranslationError.class, () -> Translation.validate(schema, policy, entity, invariants));
    }

    @ParameterizedTest
    @ValueSource(strings = { "entities" })
    void supported(String name) {
        Path entity = TestUtil.findFile(TESTDIR, name + ".json"),
                schema = TestUtil.findFile(TESTDIR, ".cedarschema"),
                policy = TestUtil.findFile(TESTDIR, ".cedar"),
                invariants = TestUtil.findFile(TESTDIR, ".invariant");
        assertDoesNotThrow(() -> Translation.validate(schema, policy, entity, invariants));
    }
}
