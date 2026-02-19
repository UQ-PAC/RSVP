package uq.pac.rsvp.policy.ast.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cedarpolicy.model.exception.InternalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType.AttributeTypeDeserialiser;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.ExtensionType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType.PrimitiveTypeDeserialiser;

public class SchemaVisitorImplTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(AttributeType.class, new AttributeTypeDeserialiser())
                .registerTypeAdapter(PrimitiveType.class, new PrimitiveTypeDeserialiser())
                .create();
    }

    static class TestVisitor extends SchemaVisitorImpl {

        int typeRefs = 0, extensions = 0, longs = 0, strings = 0, bools = 0;

        @Override
        public void visitEntityOrCommonAttributeType(EntityOrCommonType type) {
            typeRefs++;
        }

        @Override
        public void visitExtensionAttributeType(ExtensionType type) {
            extensions++;
        }

        @Override
        public void visitPrimitiveAttributeType(PrimitiveType type) {
            switch (type.getType()) {
                case Boolean:
                    bools++;
                    break;
                case Long:
                    longs++;
                    break;
                case String:
                    strings++;
                    break;
            }
        }
    }

    @Test
    void testVisitor() throws JsonMappingException, JsonProcessingException, InternalException, NullPointerException,
            IllegalStateException, IOException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema.json");
        String json = Files.readString(Path.of(url.getPath()));
        TestVisitor visitor = new TestVisitor();
        Schema schema = gson.fromJson(json, Schema.class);
        visitor.visitSchema(schema);
        assertEquals(4, visitor.typeRefs);
        assertEquals(0, visitor.extensions);
        assertEquals(1, visitor.longs);
        assertEquals(1, visitor.strings);
        assertEquals(0, visitor.bools);

        url = ClassLoader.getSystemResource("collection-types.cedarschema.json");
        json = Files.readString(Path.of(url.getPath()));
        visitor = new TestVisitor();
        schema = gson.fromJson(json, Schema.class);
        visitor.visitSchema(schema);
        assertEquals(1, visitor.typeRefs);
        assertEquals(0, visitor.extensions);
        assertEquals(0, visitor.longs);
        assertEquals(2, visitor.strings);
        assertEquals(1, visitor.bools);
    }

}
