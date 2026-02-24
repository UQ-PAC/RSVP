package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cedarpolicy.model.exception.InternalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition.CommonTypeDefinitionDeserialiser;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.common.DecimalType;
import uq.pac.rsvp.policy.ast.schema.common.DurationType;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.ast.schema.common.UnresolvedTypeReference;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

@DisplayName("Schema AST")
public class SchemaTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                .create();
    }

    @Nested
    @DisplayName("Parse Cedar schema")
    class TestCedarParsing {

        @Test
        @DisplayName("parses healthcare app correctly")
        public void healthcareApp()
                throws IOException, URISyntaxException, InternalException, NullPointerException, IllegalStateException {
            URL url = ClassLoader.getSystemResource("healthcare.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkHealthCareSchema(schema);
        }

        @Test
        @DisplayName("handles collection types")
        public void collections()
                throws IOException, URISyntaxException, InternalException, NullPointerException, IllegalStateException {
            URL url = ClassLoader.getSystemResource("collection-types.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles reserved types")
        public void reservedTypes()
                throws IOException, URISyntaxException, InternalException,
                NullPointerException, IllegalStateException {
            URL url = ClassLoader.getSystemResource("reserved-types.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkReservedTypes(schema);

        }

        @Test
        @DisplayName("handles missing namespace")
        public void missingNamespace() throws JsonMappingException, JsonProcessingException, InternalException,
                NullPointerException, IllegalStateException, IOException {
            URL url = ClassLoader.getSystemResource("missing-namespace.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkEmptyNamespace(schema);
        }

        @Test
        @DisplayName("handles circular references")
        public void circularReference() throws JsonMappingException,
                JsonProcessingException, InternalException,
                NullPointerException, IllegalStateException, IOException {
            URL url = ClassLoader.getSystemResource("circular-reference.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));
            checkCircularReference(schema);
        }

        @Test
        @DisplayName("throws parser errors")
        public void parseError() throws JsonMappingException,
                JsonProcessingException, InternalException,
                NullPointerException, IllegalStateException, IOException {
            URL url = ClassLoader.getSystemResource("parse-error.cedarschema");
            assertThrowsExactly(InternalException.class, () -> Schema.parseCedarSchema(Path.of(url.getPath())));
        }

        @Test
        @DisplayName("throws illegal shadowing errors")
        public void illegalShadow() throws JsonMappingException,
                JsonProcessingException, InternalException,
                NullPointerException, IllegalStateException, IOException {
            URL url = ClassLoader.getSystemResource("illegal-shadow.cedarschema");
            assertThrowsExactly(InternalException.class, () -> Schema.parseCedarSchema(Path.of(url.getPath())));
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() throws JsonMappingException,
                JsonProcessingException, InternalException,
                NullPointerException, IllegalStateException, IOException {
            URL url = ClassLoader.getSystemResource("legal-shadow.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));
            checkShadowing(schema);
        }
    }

    @Nested
    @DisplayName("Parse JSON schema")
    class TestJSONParsing {

        @Test
        @DisplayName("parses healthcare app correctly")
        public void healthcareApp() throws IOException {
            URL url = ClassLoader.getSystemResource("healthcare.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = gson.fromJson(json, Schema.class);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkHealthCareSchema(schema);
        }

        @Test
        @DisplayName("handles collection types")
        public void collections() throws IOException {
            URL url = ClassLoader.getSystemResource("collection-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = gson.fromJson(json, Schema.class);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles reserved types")
        public void reservedTypes()
                throws IOException {
            URL url = ClassLoader.getSystemResource("reserved-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = gson.fromJson(json, Schema.class);

            checkReservedTypes(schema);
        }

        @Test
        @DisplayName("handles empty namespace")
        public void emptyNamespace() throws IOException {
            URL url = ClassLoader.getSystemResource("empty-namespace.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = gson.fromJson(json, Schema.class);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkEmptyNamespace(schema);
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() throws IOException {
            URL url = ClassLoader.getSystemResource("legal-shadow.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = gson.fromJson(json, Schema.class);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkShadowing(schema);
        }
    }

    @Nested
    @DisplayName("Construct schema manually")
    class TestManual {

        @Test
        @DisplayName("handles collection types")
        public void collections() throws IOException {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> entityTypes = new HashMap<>();
            Map<String, ActionDefinition> actions = new HashMap<>();
            Map<String, CommonTypeDefinition> commonTypes = new HashMap<>();

            Map<String, CommonTypeDefinition> userShape = new HashMap<>();

            userShape.put("nicknames", new SetTypeDefinition(new StringType()));

            Map<String, CommonTypeDefinition> preferences = new HashMap<>();
            preferences.put("diet", new StringType());
            preferences.put("colour", new UnresolvedTypeReference("Colour"));

            userShape.put("preferences", new RecordTypeDefinition(preferences));

            userShape.put("consented", new BooleanType(false));
            entityTypes.put("User", new EntityTypeDefinition(new HashSet<>(), userShape));

            Set<String> colourEnum = Set.copyOf(Arrays.asList("red", "green", "blue", "purple", "pink", "yellow"));
            entityTypes.put("Colour", new EntityTypeDefinition(new HashSet<>(), new HashMap<>(),
                    colourEnum, new HashMap<>()));

            Namespace dataCollection = new Namespace(entityTypes, actions, commonTypes);
            schema.put("DataCollectionApp", dataCollection);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles circular references")
        public void circularReference() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> entityTypes = new HashMap<>();
            Map<String, ActionDefinition> actions = new HashMap<>();
            Map<String, CommonTypeDefinition> commonTypes = new HashMap<>();

            Map<String, CommonTypeDefinition> accountShape = new HashMap<>();

            accountShape.put("friend", new UnresolvedTypeReference("Account"));
            entityTypes.put("Account", new EntityTypeDefinition(new HashSet<>(), accountShape));

            Namespace app = new Namespace(entityTypes, actions, commonTypes);
            schema.put("App", app);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkCircularReference(schema);
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> oneEntities = new HashMap<>();
            oneEntities.put("A", new EntityTypeDefinition(new HashSet<>(), new HashMap<>()));

            Namespace one = new Namespace(oneEntities, new HashMap<>(), new HashMap<>());
            schema.put("One", one);

            Map<String, EntityTypeDefinition> twoEntities = new HashMap<>();

            twoEntities.put("A", new EntityTypeDefinition(new HashSet<>(), new HashMap<>()));

            Map<String, CommonTypeDefinition> shape = new HashMap<>();

            shape.put("a", new UnresolvedTypeReference("One::A"));
            shape.put("b", new UnresolvedTypeReference("Two::A"));
            shape.put("c", new UnresolvedTypeReference("A"));
            twoEntities.put("B", new EntityTypeDefinition(new HashSet<>(), shape));

            Namespace two = new Namespace(twoEntities, new HashMap<>(), new HashMap<>());
            schema.put("Two", two);

            new SchemaResolutionVisitor().visitSchema(schema);

            checkShadowing(schema);
        }

    }

    private void checkHealthCareSchema(Schema schema) {
        assertEquals(1, schema.size());

        Namespace healthcareApp = schema.get("HealthCareApp");
        assertNotNull(healthcareApp);
        assertEquals("HealthCareApp", healthcareApp.getName());

        assertEquals(4, healthcareApp.entityTypeNames().size());
        assertTrue(healthcareApp.entityTypeNames().containsAll(Arrays.asList("Role", "User", "InfoType", "Info")));
        assertEquals(4, schema.entityTypeNames().size());
        assertTrue(schema.entityTypeNames().containsAll(Arrays.asList("HealthCareApp::Role", "HealthCareApp::User",
                "HealthCareApp::InfoType", "HealthCareApp::Info")));

        EntityTypeDefinition role = healthcareApp.getEntityType("Role");
        EntityTypeDefinition user = healthcareApp.getEntityType("User");
        EntityTypeDefinition infoType = healthcareApp.getEntityType("InfoType");
        EntityTypeDefinition info = healthcareApp.getEntityType("Info");

        assertEquals(infoType, Schema.resolveEntityType("HealthCareApp::InfoType", schema, healthcareApp));
        assertEquals(infoType, schema.getEntityType("HealthCareApp::InfoType"));

        assertEquals("HealthCareApp::Role", role.getName());
        assertEquals("HealthCareApp::User", user.getName());
        assertEquals("HealthCareApp::InfoType", infoType.getName());
        assertEquals("HealthCareApp::Info", info.getName());

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
        assertEquals(4, schema.actionNames().size());

        assertTrue(healthcareApp.actionNames().containsAll(
                Arrays.asList("createAppointment", "updateAppointment", "deleteAppointment", "listAppointments")));
        assertTrue(schema.actionNames()
                .containsAll(Arrays.asList("HealthCareApp::Action::createAppointment",
                        "HealthCareApp::Action::updateAppointment", "HealthCareApp::Action::deleteAppointment",
                        "HealthCareApp::Action::listAppointments")));

        ActionDefinition create = healthcareApp.getAction("createAppointment");
        ActionDefinition update = healthcareApp.getAction("updateAppointment");
        ActionDefinition delete = healthcareApp.getAction("deleteAppointment");
        ActionDefinition list = healthcareApp.getAction("listAppointments");

        assertEquals(create,
                Schema.resolveActionType("createAppointment", "HealthCareApp::Action", schema, healthcareApp));
        assertEquals(create, schema.getAction("HealthCareApp::Action::createAppointment"));

        assertEquals("HealthCareApp::Action::createAppointment", create.getName());
        assertEquals("HealthCareApp::Action::updateAppointment", update.getName());
        assertEquals("HealthCareApp::Action::deleteAppointment", delete.getName());
        assertEquals("HealthCareApp::Action::listAppointments", list.getName());

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
        assertEquals(1, schema.commonTypeNames().size());
        assertTrue(healthcareApp.commonTypeNames().contains("AppointmentDetails"));
        assertTrue(schema.commonTypeNames().contains("HealthCareApp::AppointmentDetails"));

        CommonTypeDefinition details = healthcareApp.getCommonType("AppointmentDetails");
        assertTrue(details instanceof RecordTypeDefinition);

        assertEquals(details, Schema.resolveCommonType("HealthCareApp::AppointmentDetails", schema, healthcareApp));
        assertEquals(details, schema.getCommonType("HealthCareApp::AppointmentDetails"));

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

    private void checkEmptyNamespace(Schema schema) {
        Namespace namespace = schema.get("");
        assertEquals(1, namespace.entityTypeNames().size());
        assertEquals(1, namespace.actionNames().size());
        assertEquals(0, namespace.commonTypeNames().size());

        EntityTypeDefinition person = namespace.getEntityType("Person");
        assertNotNull(person);

        ActionDefinition slap = namespace.getAction("slap");
        assertNotNull(slap);

        Set<EntityTypeDefinition> principals = slap.getAppliesToPrincipalTypes();
        Set<EntityTypeDefinition> resources = slap.getAppliesToResourceTypes();

        assertEquals(1, principals.size());
        assertEquals(1, resources.size());

        assertTrue(principals.contains(person));
        assertTrue(resources.contains(person));

    }

    private void checkCircularReference(Schema schema) {
        Namespace app = schema.get("App");
        assertEquals(1, app.entityTypeNames().size());
        assertEquals(0, app.actionNames().size());
        assertEquals(0, app.commonTypeNames().size());

        EntityTypeDefinition account = app.getEntityType("Account");
        assertNotNull(account);

        CommonTypeDefinition friend = account.getShapeAttributeType("friend");
        assertNotNull(friend);

        assertTrue(friend instanceof EntityTypeReference);
        assertEquals(account, ((EntityTypeReference) friend).getDefinition());
    }

    private void checkReservedTypes(Schema schema) {
        Namespace namespace = schema.get("");
        assertEquals(2, namespace.entityTypeNames().size());
        assertEquals(0, namespace.actionNames().size());
        assertEquals(0, namespace.commonTypeNames().size());

        EntityTypeDefinition person = namespace.getEntityType("Person");
        EntityTypeDefinition sleep = namespace.getEntityType("Sleep");
        assertNotNull(person);
        assertNotNull(sleep);

        CommonTypeDefinition name = person.getShapeAttributeType("name");
        CommonTypeDefinition age = person.getShapeAttributeType("age");
        CommonTypeDefinition worth = person.getShapeAttributeType("worth");
        CommonTypeDefinition bedtime = person.getShapeAttributeType("bedtime");
        CommonTypeDefinition address = person.getShapeAttributeType("address");
        assertNotNull(name);
        assertNotNull(age);
        assertNotNull(worth);
        assertNotNull(bedtime);
        assertNotNull(address);

        assertTrue(name instanceof StringType);
        assertTrue(age instanceof LongType);
        assertTrue(worth instanceof DecimalType);
        assertTrue(bedtime instanceof DateTimeType);
        assertTrue(address instanceof IpAddressType);

        CommonTypeDefinition duration = sleep.getShapeAttributeType("duration");
        CommonTypeDefinition deep = sleep.getShapeAttributeType("deep");
        assertNotNull(duration);
        assertNotNull(deep);

        assertTrue(duration instanceof DurationType);
        assertTrue(deep instanceof BooleanType);
    }

    private void checkShadowing(Schema schema) {
        Namespace one = schema.get("One");
        Namespace two = schema.get("Two");

        EntityTypeDefinition oneA = one.getEntityType("A");
        assertNotNull(oneA);
        assertEquals("One::A", oneA.getName());

        EntityTypeDefinition twoA = two.getEntityType("A");
        assertNotNull(twoA);
        assertEquals("Two::A", twoA.getName());

        EntityTypeDefinition twoB = two.getEntityType("B");
        assertNotNull(twoB);
        assertEquals("Two::B", twoB.getName());

        assertEquals(oneA, schema.getEntityType("One::A"));
        assertEquals(twoA, schema.getEntityType("Two::A"));
        assertEquals(twoB, schema.getEntityType("Two::B"));

        CommonTypeDefinition a = twoB.getShapeAttributeType("a");
        CommonTypeDefinition b = twoB.getShapeAttributeType("b");
        CommonTypeDefinition c = twoB.getShapeAttributeType("c");
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);

        assertTrue(a instanceof EntityTypeReference);
        assertTrue(b instanceof EntityTypeReference);
        assertTrue(c instanceof EntityTypeReference);

        assertEquals(oneA, ((EntityTypeReference) a).getDefinition());
        assertEquals(twoA, ((EntityTypeReference) b).getDefinition());
        assertEquals(twoA, ((EntityTypeReference) c).getDefinition());
    }
}
