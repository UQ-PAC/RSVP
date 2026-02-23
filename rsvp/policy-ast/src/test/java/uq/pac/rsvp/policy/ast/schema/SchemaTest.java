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

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.common.UnresolvedTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

public class SchemaTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                .create();
    }

    private void checkHealthCareSchema(Schema schema) {
        assertEquals(1, schema.size());

        Namespace healthcareApp = schema.get("HealthCareApp");
        assertNotNull(healthcareApp);
        assertEquals("HealthCareApp", healthcareApp.getName());

        assertEquals(4, healthcareApp.entityTypeNames().size());
        assertTrue(healthcareApp.entityTypeNames().containsAll(Arrays.asList("Role", "User", "InfoType", "Info")));

        EntityTypeDefinition role = healthcareApp.getEntityType("Role");
        EntityTypeDefinition user = healthcareApp.getEntityType("User");
        EntityTypeDefinition infoType = healthcareApp.getEntityType("InfoType");
        EntityTypeDefinition info = healthcareApp.getEntityType("Info");

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

        CommonTypeDefinition provider = info.getShapeAttributeType("provider");
        CommonTypeDefinition patient = info.getShapeAttributeType("patient");

        assertTrue(provider.isResolved());
        assertTrue(patient.isResolved());

        assertTrue(provider instanceof EntityTypeReference);
        assertTrue(patient instanceof EntityTypeReference);

        assertEquals(user, ((EntityTypeReference) provider).getDefinition());
        assertEquals(user, ((EntityTypeReference) patient).getDefinition());

        assertEquals(4, healthcareApp.actionNames().size());
        assertTrue(healthcareApp.actionNames()
                .containsAll(Arrays.asList("createAppointment", "updateAppointment", "listAppointments")));

        ActionDefinition create = healthcareApp.getAction("createAppointment");
        ActionDefinition update = healthcareApp.getAction("updateAppointment");
        ActionDefinition delete = healthcareApp.getAction("deleteAppointment");
        ActionDefinition list = healthcareApp.getAction("listAppointments");

        assertEquals(create,
                Schema.resolveActionType("createAppointment", "HealthCareApp::Action", schema, healthcareApp));

        assertEquals("createAppointment", create.getName());
        assertEquals("updateAppointment", update.getName());
        assertEquals("deleteAppointment", delete.getName());
        assertEquals("listAppointments", list.getName());

        assertEquals(1, create.getMemberOf().size());
        assertEquals(0, update.getMemberOf().size());
        assertEquals(0, delete.getMemberOf().size());
        assertEquals(0, list.getMemberOf().size());

        assertTrue(create.getMemberOf().contains(update));

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

        CommonTypeDefinition referrer = create.getAppliesToContext().getAttributeType("referrer");

        assertTrue(referrer.isResolved());
        assertTrue(referrer instanceof EntityTypeReference);
        assertEquals("referrer", referrer.getName());
        assertEquals(user, ((EntityTypeReference) referrer).getDefinition());

        assertEquals(1, healthcareApp.commonTypeNames().size());
        assertTrue(healthcareApp.commonTypeNames().contains("AppointmentDetails"));

        CommonTypeDefinition details = healthcareApp.getCommonType("AppointmentDetails");
        assertTrue(details instanceof RecordTypeDefinition);

        assertEquals(details, Schema.resolveCommonType("HealthCareApp::AppointmentDetails", schema, healthcareApp));

        CommonTypeDefinition detailAttr = create.getAppliesToContext().getAttributeType("detail");

        assertTrue(detailAttr.isResolved());
        assertTrue(detailAttr instanceof CommonTypeReference);
        assertEquals(details, ((CommonTypeReference) detailAttr).getDefinition());
    }

    private void checkCollectionSchema(Schema schema) {
        Namespace dataApp = schema.get("DataCollectionApp");
        assertEquals(2, dataApp.entityTypeNames().size());
        assertEquals(0, dataApp.actionNames().size());
        assertEquals(0, dataApp.commonTypeNames().size());

        EntityTypeDefinition user = dataApp.getEntityType("User");
        EntityTypeDefinition colour = dataApp.getEntityType("Colour");
        assertNotNull(user);
        assertNotNull(colour);

        CommonTypeDefinition nicknames = user.getShapeAttributeType("nicknames");
        CommonTypeDefinition preferences = user.getShapeAttributeType("preferences");

        assertTrue(nicknames instanceof SetTypeDefinition);
        assertTrue(preferences instanceof RecordTypeDefinition);

        assertTrue(
                ((RecordTypeDefinition) preferences).getAttributeNames().containsAll(Arrays.asList("diet", "colour")));
    }

    @Test
    public void testDeserialisation() throws IOException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema.json");
        String json = Files.readString(Path.of(url.getPath()));
        Schema schema = gson.fromJson(json, Schema.class);

        SchemaVisitor visitor = new SchemaResolutionVisitor();
        visitor.visitSchema(schema);

        checkHealthCareSchema(schema);

        url = ClassLoader.getSystemResource("collection-types.cedarschema.json");
        json = Files.readString(Path.of(url.getPath()));
        schema = gson.fromJson(json, Schema.class);

        visitor = new SchemaResolutionVisitor();
        visitor.visitSchema(schema);

        checkCollectionSchema(schema);
    }

    @Test
    public void testCedarParsing()
            throws IOException, URISyntaxException, InternalException, NullPointerException, IllegalStateException {
        URL url = ClassLoader.getSystemResource("healthcare.cedarschema");
        Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

        checkHealthCareSchema(schema);

        url = ClassLoader.getSystemResource("collection-types.cedarschema");
        schema = Schema.parseCedarSchema(Path.of(url.getPath()));

        checkCollectionSchema(schema);
    }

}
