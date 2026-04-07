package uq.pac.rsvp.policy.ast.schema.common;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.JsonParser;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Schema record type AST")
public class RecordTypeDefinitionTest {

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
        @DisplayName("Handles no attributes")
        void empty() {
            RecordTypeDefinition empty = gson.fromJson("{}", RecordTypeDefinition.class);
            assertInstanceOf(RecordTypeDefinition.class, empty);
            assertEquals(0, empty.getAttributes().size());

            empty = gson.fromJson("{ \"type\": \"Record\" }", RecordTypeDefinition.class);
            assertInstanceOf(RecordTypeDefinition.class, empty);
            assertEquals(0, empty.getAttributes().size());

            empty = gson.fromJson("{ \"type\": \"Record\", \"attributes\": {} }", RecordTypeDefinition.class);
            assertInstanceOf(RecordTypeDefinition.class, empty);
            assertEquals(0, empty.getAttributes().size());
        }

        private RecordTypeDefinition.Attribute getAttribute(RecordTypeDefinition def, String attr) {
            return def.getAttributes().keySet().stream()
                    .filter(a -> a.getName().equals(attr))
                    .findFirst().orElseThrow();
        }

        @Test
        @DisplayName("Handles required")
        void required() throws IOException {
            URL url = ClassLoader.getSystemResource("required-attr.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            EntityTypeDefinition something = schema.getEntityType("App::Something");
            CommonTypeDefinition sometype = schema.getCommonType("App::SomeType");

            assertNotNull(something);
            assertNotNull(sometype);
            assertInstanceOf(RecordTypeDefinition.class, sometype);

            assertFalse(getAttribute((RecordTypeDefinition) sometype, "optional").isRequired());
            assertTrue(getAttribute((RecordTypeDefinition) sometype, "default").isRequired());
            assertTrue(getAttribute((RecordTypeDefinition) sometype, "explicit").isRequired());
        }
    }

    @Nested
    @DisplayName("Construct records manually")
    class TestManual {

        @Test
        @DisplayName("Handles no attributes")
        void empty() {
            RecordTypeDefinition empty = new RecordTypeDefinition();
            assertEquals(0, empty.getAttributes().size());

            empty = new RecordTypeDefinition();
            assertEquals(0, empty.getAttributes().size());
        }
    }
}
