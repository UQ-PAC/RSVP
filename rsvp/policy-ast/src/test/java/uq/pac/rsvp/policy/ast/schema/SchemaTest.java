package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cedarpolicy.model.exception.InternalException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType;
import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType.AttributeTypeDeserialiser;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType.PrimitiveTypeDeserialiser;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

public class SchemaTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(AttributeType.class, new AttributeTypeDeserialiser())
                .registerTypeAdapter(PrimitiveType.class, new PrimitiveTypeDeserialiser())
                .create();
    }

    private void checkHealthCareSchema(Schema schema) {
        assertEquals(1, schema.size());

        Namespace healthcareApp = schema.get("HealthCareApp");
        assertNotNull(healthcareApp);
        assertEquals("HealthCareApp", healthcareApp.getName());

        assertEquals(4, healthcareApp.entityTypeNames().size());
        assertTrue(healthcareApp.entityTypeNames().containsAll(Arrays.asList("Role", "User", "InfoType", "Info")));

        EntityType role = healthcareApp.getEntityType("Role");
        EntityType user = healthcareApp.getEntityType("User");
        EntityType infoType = healthcareApp.getEntityType("InfoType");
        EntityType info = healthcareApp.getEntityType("Info");

        assertEquals(infoType, Schema.resolveEntityType("HealthCareApp::InfoType", schema, healthcareApp));

        assertEquals("Role", role.getName());
        assertEquals("User", user.getName());
        assertEquals("InfoType", infoType.getName());
        assertEquals("Info", info.getName());

        assertEquals(3, role.getEntityNamesEnum().size());
        assertEquals(0, user.getEntityNamesEnum().size());
        assertEquals(0, infoType.getEntityNamesEnum().size());
        assertEquals(0, info.getEntityNamesEnum().size());

        assertTrue(role.getEntityNamesEnum().containsAll(Arrays.asList("Admin", "Doctor", "InsuranceRep")));

        assertEquals(0, role.getMemberOfTypes().size());
        assertEquals(1, user.getMemberOfTypes().size());
        assertEquals(0, infoType.getMemberOfTypes().size());
        assertEquals(1, info.getMemberOfTypes().size());

        assertTrue(user.getMemberOfTypes().contains(role));
        assertTrue(info.getMemberOfTypes().contains(infoType));

        assertEquals(0, role.getShapeAttributeNames().size());
        assertEquals(0, user.getShapeAttributeNames().size());
        assertEquals(0, infoType.getShapeAttributeNames().size());
        assertEquals(2, info.getShapeAttributeNames().size());

        AttributeType provider = info.getShapeAttributeType("provider");
        AttributeType patient = info.getShapeAttributeType("patient");

        assertTrue(provider instanceof EntityOrCommonType);
        assertTrue(patient instanceof EntityOrCommonType);

        assertTrue(((EntityOrCommonType) provider).isResolved());
        assertTrue(((EntityOrCommonType) patient).isResolved());

        assertEquals(user, ((EntityOrCommonType) provider).getEntityTypeOrNull());
        assertEquals(user, ((EntityOrCommonType) patient).getEntityTypeOrNull());

        assertEquals(4, healthcareApp.actionNames().size());
        assertTrue(healthcareApp.actionNames()
                .containsAll(Arrays.asList("createAppointment", "updateAppointment", "listAppointments")));

        Action create = healthcareApp.getAction("createAppointment");
        Action update = healthcareApp.getAction("updateAppointment");
        Action delete = healthcareApp.getAction("deleteAppointment");
        Action list = healthcareApp.getAction("listAppointments");

        assertEquals(create,
                Schema.resolveActionType("createAppointment", "HealthCareApp::Action", schema, healthcareApp));

        assertEquals("createAppointment", create.getName());
        assertEquals("updateAppointment", update.getName());
        assertEquals("deleteAppointment", delete.getName());
        assertEquals("listAppointments", list.getName());

        assertEquals(1, create.getMemberOfActions().size());
        assertEquals(0, update.getMemberOfActions().size());
        assertEquals(0, delete.getMemberOfActions().size());
        assertEquals(0, list.getMemberOfActions().size());

        assertTrue(create.getMemberOfActions().contains(update));

        assertEquals(1, create.getAppliesToPrincipalTypes().size());
        assertEquals(1, update.getAppliesToPrincipalTypes().size());
        assertEquals(1, delete.getAppliesToPrincipalTypes().size());
        assertEquals(1, list.getAppliesToPrincipalTypes().size());

        assertTrue(create.getAppliesToPrincipalTypes().contains(user));
        assertTrue(update.getAppliesToPrincipalTypes().contains(user));
        assertTrue(delete.getAppliesToPrincipalTypes().contains(user));
        assertTrue(list.getAppliesToPrincipalTypes().contains(user));

        assertEquals(1, create.getAppliesToResourceTypes().size());
        assertEquals(1, update.getAppliesToResourceTypes().size());
        assertEquals(1, delete.getAppliesToResourceTypes().size());
        assertEquals(1, list.getAppliesToResourceTypes().size());

        assertTrue(create.getAppliesToResourceTypes().contains(info));
        assertTrue(update.getAppliesToResourceTypes().contains(info));
        assertTrue(delete.getAppliesToResourceTypes().contains(info));
        assertTrue(list.getAppliesToResourceTypes().contains(info));

        assertEquals(2, create.getAppliesToContext().getAttributeNames().size());
        assertNull(update.getAppliesToContext());
        assertNull(delete.getAppliesToContext());
        assertNull(list.getAppliesToContext());

        AttributeType referrer = create.getAppliesToContext().getAttributeType("referrer");

        assertTrue(referrer instanceof EntityOrCommonType);
        assertTrue(((EntityOrCommonType) referrer).isResolved());
        assertEquals(user, ((EntityOrCommonType) referrer).getEntityTypeOrNull());

        assertEquals(1, healthcareApp.commonTypeNames().size());
        assertTrue(healthcareApp.commonTypeNames().contains("AppointmentDetails"));

        AttributeType details = healthcareApp.getCommonType("AppointmentDetails");
        assertTrue(details instanceof RecordType);

        assertEquals(details, Schema.resolveCommonType("HealthCareApp::AppointmentDetails", schema, healthcareApp));

        AttributeType detailAttr = create.getAppliesToContext().getAttributeType("detail");

        assertTrue(detailAttr instanceof EntityOrCommonType);
        assertTrue(((EntityOrCommonType) detailAttr).isResolved());
        assertEquals(details, ((EntityOrCommonType) detailAttr).getCommonTypeOrNull());
    }

    @Test
    public void testDeserialisation() throws IOException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema.json");
        String json = Files.readString(Path.of(url.getPath()));
        Schema schema = gson.fromJson(json, Schema.class);

        SchemaVisitor visitor = new SchemaResolutionVisitor();
        visitor.visitSchema(schema);

        checkHealthCareSchema(schema);
    }

    @Test
    public void testCedarParsing()
            throws IOException, URISyntaxException, InternalException, NullPointerException, IllegalStateException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema");
        Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

        checkHealthCareSchema(schema);
    }

}
 