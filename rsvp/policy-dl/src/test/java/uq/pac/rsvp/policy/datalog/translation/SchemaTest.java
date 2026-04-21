package uq.pac.rsvp.policy.datalog.translation;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaTest {

    private final static Path TESTDIR = Path.of(TestUtil.RESOURCEDIR.toString(), "schema");

    @Test
    void unsupportedName() {
        List<Path> schemas = TestUtil.findFiles(TESTDIR, ".cedarschema");
        Path policy = TestUtil.findFile(TESTDIR, ".cedar");
        Path entities = TestUtil.findFile(TESTDIR, ".json");
        Path invariants = TestUtil.findFile(TESTDIR, ".invariant");
        for (Path schema : schemas) {
            assertThrows(TranslationError.class, () -> Translation.validate(schema, policy, entities, invariants));
        }
    }
}
