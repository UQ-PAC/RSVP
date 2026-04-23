package uq.pac.rsvp.policy.ast.schema.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.deserilisation.JsonParser;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.deserilisation.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

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
            assertTrue(empty instanceof RecordTypeDefinition);
            assertEquals(0, empty.getAttributeNames().size());

            empty = gson.fromJson("{ \"type\": \"Record\" }", RecordTypeDefinition.class);
            assertTrue(empty instanceof RecordTypeDefinition);
            assertEquals(0, empty.getAttributeNames().size());

            empty = gson.fromJson("{ \"type\": \"Record\", \"attributes\": {} }", RecordTypeDefinition.class);
            assertTrue(empty instanceof RecordTypeDefinition);
            assertEquals(0, empty.getAttributeNames().size());
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
            assertFalse(sometype.isRequired());

            assertTrue(something.getShapeAttributeNames().contains("optional"));
            assertFalse(something.getShapeAttributeType("optional").isRequired());

            assertTrue(sometype instanceof RecordTypeDefinition);

            RecordTypeDefinition record = (RecordTypeDefinition) sometype;

            assertTrue(record.getAttributeNames().contains("default"));
            assertTrue(record.getAttributeType("default").isRequired());
            assertTrue(record.getAttributeNames().contains("explicit"));
            assertTrue(record.getAttributeType("explicit").isRequired());
            assertTrue(record.getAttributeNames().contains("optional"));
            assertFalse(record.getAttributeType("optional").isRequired());
        }

    }

    @Nested
    @DisplayName("Construct records manually")
    class TestManual {

        @Test
        @DisplayName("Handles no attributes")
        void empty() {
            RecordTypeDefinition empty = new RecordTypeDefinition();
            assertEquals(0, empty.getAttributeNames().size());

            empty = new RecordTypeDefinition(null, false);
            assertEquals(0, empty.getAttributeNames().size());

            empty = new RecordTypeDefinition(Collections.emptyMap(), false);
            assertEquals(0, empty.getAttributeNames().size());
        }
    }
}
