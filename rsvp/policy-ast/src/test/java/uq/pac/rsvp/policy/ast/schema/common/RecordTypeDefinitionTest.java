package uq.pac.rsvp.policy.ast.schema.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;

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
