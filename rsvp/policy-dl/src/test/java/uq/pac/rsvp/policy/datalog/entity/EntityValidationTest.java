package uq.pac.rsvp.policy.datalog.entity;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class EntityValidationTest {

    @Test
    void test() throws RsvpException, FileNotFoundException {
        Path schemaPath = TestUtil.getResourceDir("translation", "photoapp");
        Schema schema = Schema.parseCedarSchema(Path.of(schemaPath.toString(), "photoapp.cedarschema"));
        EntitySet entities = EntitySet.parse(Path.of(schemaPath.toString(), "entities.json"));
    }
}
