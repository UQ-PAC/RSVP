package uq.pac.rsvp.policy.ast.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cedarpolicy.model.exception.InternalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.common.DecimalType;
import uq.pac.rsvp.policy.ast.schema.common.DurationType;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.StringType;

public class SchemaVisitorImplTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                .create();
    }

    static class TestVisitor extends SchemaVisitorImpl {

        int entities = 0, commonTypes = 0, datetimes = 0, decimals = 0, durations = 0, ipaddrs = 0, longs = 0,
                strings = 0, bools = 0;

        @Override
        public void visitEntityTypeReference(EntityTypeReference type) {
            entities++;
        }

        @Override
        public void visitCommonTypeReference(CommonTypeReference type) {
            commonTypes++;
        }

        @Override
        public void visitBoolean(BooleanType type) {
            bools++;
        }

        @Override
        public void visitLong(LongType type) {
            longs++;
        }

        @Override
        public void visitString(StringType type) {
            strings++;
        }

        @Override
        public void visitDateTime(DateTimeType type) {
            datetimes++;
        }

        @Override
        public void visitDecimal(DecimalType type) {
            decimals++;
        }

        @Override
        public void visitDuration(DurationType type) {
            durations++;
        }

        @Override
        public void visitIpAddress(IpAddressType type) {
            ipaddrs++;
        }

    }

    @Test
    void testVisitor() throws JsonMappingException, JsonProcessingException, InternalException, NullPointerException,
            IllegalStateException, IOException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema.json");
        Schema schema = Schema.parseJsonSchema(Path.of(url.getPath()));
        TestVisitor visitor = new TestVisitor();
        visitor.visitSchema(schema);
        assertEquals(3, visitor.entities);
        assertEquals(1, visitor.commonTypes);
        assertEquals(0, visitor.datetimes);
        assertEquals(0, visitor.decimals);
        assertEquals(0, visitor.durations);
        assertEquals(0, visitor.ipaddrs);
        assertEquals(1, visitor.longs);
        assertEquals(1, visitor.strings);
        assertEquals(0, visitor.bools);

        url = ClassLoader.getSystemResource("collection-types.cedarschema.json");
        visitor = new TestVisitor();
        schema = Schema.parseJsonSchema(Path.of(url.getPath()));
        visitor.visitSchema(schema);
        assertEquals(1, visitor.entities);
        assertEquals(0, visitor.commonTypes);
        assertEquals(0, visitor.datetimes);
        assertEquals(0, visitor.decimals);
        assertEquals(0, visitor.durations);
        assertEquals(0, visitor.ipaddrs);
        assertEquals(0, visitor.longs);
        assertEquals(2, visitor.strings);
        assertEquals(1, visitor.bools);
    }

}
