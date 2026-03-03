package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uq.pac.rsvp.policy.ast.JsonParser;
import uq.pac.rsvp.policy.ast.schema.common.StringType;

@DisplayName("Schema namespace AST")
public class NamespaceTest {

    @Nested
    @DisplayName("Parse JSON")
    class TestJSON {

        @Test
        @DisplayName("handles empty namespace")
        public void emptyNamespace() throws IOException {
            URL url = ClassLoader.getSystemResource("empty-namespace.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            Namespace app = schema.get("App");

            assertEquals("App", app.getName());

            assertEquals(0, app.entityTypeNames().size());
            assertEquals(0, app.actionNames().size());
            assertEquals(0, app.commonTypeNames().size());

            app.resolveCommonType("SomeType", new StringType());

            Namespace lib = schema.get("Lib");

            assertEquals(1, lib.entityTypeNames().size());
            assertEquals(0, lib.actionNames().size());
            assertEquals(0, lib.commonTypeNames().size());

        }
    }

    @Nested
    @DisplayName("Construct namespace manually")
    class TestManual {

        @Test
        @DisplayName("handles empty namespace")
        public void emptyNamespace() {
            Namespace namespace = new Namespace();

            assertEquals("", namespace.getName());

            assertEquals(0, namespace.entityTypeNames().size());
            assertEquals(0, namespace.actionNames().size());
            assertEquals(0, namespace.commonTypeNames().size());

            Namespace other = new Namespace("App", new HashMap<>(), new HashMap<>());

            assertEquals(0, other.entityTypeNames().size());
            assertEquals(0, other.actionNames().size());
            assertEquals(0, other.commonTypeNames().size());

            other.resolveCommonType("SomeType", new StringType());
        }

    }
}
