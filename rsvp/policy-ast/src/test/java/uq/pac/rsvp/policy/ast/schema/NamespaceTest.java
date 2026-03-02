package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.common.StringType;

@DisplayName("Schema namespace AST")
public class NamespaceTest {

    @Nested
    @DisplayName("Parse JSON")
    class TestJSON {

        static Gson gson;

        @BeforeAll
        static void beforeAll() {
            gson = new GsonBuilder()
                    .registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                    .disableJdkUnsafe()
                    .create();
        }

        @Test
        @DisplayName("handles empty namespace")
        public void emptyNamespace() throws IOException {
            URL url = ClassLoader.getSystemResource("empty-namespace.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = gson.fromJson(json, Schema.class);

            Namespace app = schema.get("App");

            assertNull(app.getName());

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

            assertNull(namespace.getName());

            assertEquals(0, namespace.entityTypeNames().size());
            assertEquals(0, namespace.actionNames().size());
            assertEquals(0, namespace.commonTypeNames().size());

            Namespace other = new Namespace(new HashMap<>(), new HashMap<>());

            assertEquals(0, other.entityTypeNames().size());
            assertEquals(0, other.actionNames().size());
            assertEquals(0, other.commonTypeNames().size());

            other.resolveCommonType("SomeType", new StringType());
        }

    }
}
