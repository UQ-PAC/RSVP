package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.RsvpException;

import java.net.URL;
import java.nio.file.Path;

public class SchemaDifferentialTest {

    @Test
    void test() throws RsvpException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema");
        Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));
    }
}
