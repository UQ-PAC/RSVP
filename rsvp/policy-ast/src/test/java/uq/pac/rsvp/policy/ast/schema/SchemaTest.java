package uq.pac.rsvp.policy.ast.schema;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.JsonParser;
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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Schema AST")
public class SchemaTest {

    @Nested
    @DisplayName("Parse Cedar schema")
    class TestCedarParsing {

        @Test
        @DisplayName("parses healthcare app correctly")
        public void healthcareApp() throws RsvpException {
            URL url = ClassLoader.getSystemResource("healthcare.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkHealthCareSchema(schema);
        }

        @Test
        @DisplayName("handles collection types")
        public void collections() throws RsvpException {
            URL url = ClassLoader.getSystemResource("collection-types.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles reserved types")
        public void reservedTypes() throws RsvpException {
            URL url = ClassLoader.getSystemResource("reserved-types.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkReservedTypes(schema);

        }

        @Test
        @DisplayName("handles missing namespace")
        public void missingNamespace() throws RsvpException {
            URL url = ClassLoader.getSystemResource("missing-namespace.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            checkEmptyNamespace(schema);
        }

        @Test
        @DisplayName("handles circular references")
        public void circularReference() throws RsvpException {
            URL url = ClassLoader.getSystemResource("circular-reference.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));
            checkCircularReference(schema);
        }

        @Test
        @DisplayName("throws parser errors")
        public void parseError() {
            URL url = ClassLoader.getSystemResource("parse-error.cedarschema");
            assertThrowsExactly(RsvpException.class, () -> Schema.parseCedarSchema(Path.of(url.getPath())));
        }

        @Test
        @DisplayName("throws illegal shadowing errors")
        public void illegalShadow() {
            URL url = ClassLoader.getSystemResource("illegal-shadow.cedarschema");
            assertThrowsExactly(RsvpException.class, () -> Schema.parseCedarSchema(Path.of(url.getPath())));
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() throws RsvpException {
            URL url = ClassLoader.getSystemResource("legal-shadow.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));
            checkShadowing(schema);
        }

        @Test
        @DisplayName("handles annotations")
        public void annotations() throws RsvpException {
            URL url = ClassLoader.getSystemResource("type-annotations.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));
            checkAnnotations(schema);
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
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkHealthCareSchema(schema);
        }

        @Test
        @DisplayName("handles collection types")
        public void collections() throws IOException {
            URL url = ClassLoader.getSystemResource("collection-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles reserved types")
        public void reservedTypes()
                throws IOException {
            URL url = ClassLoader.getSystemResource("reserved-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            checkReservedTypes(schema);
        }

        @Test
        @DisplayName("handles unnamed namespace")
        public void emptyNamespace() throws IOException {
            URL url = ClassLoader.getSystemResource("unnamed-namespace.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkEmptyNamespace(schema);
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() throws IOException {
            URL url = ClassLoader.getSystemResource("legal-shadow.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkShadowing(schema);
        }

        @Test
        @DisplayName("handles resolved type format")
        public void invalidTypes() throws IOException {
            URL url = ClassLoader.getSystemResource("invalid-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            assertThrows(SchemaResolutionException.class, () -> schema.accept(new SchemaResolutionVisitor()));
        }

        @Test
        @DisplayName("handles annotations")
        public void annotations() throws IOException {
            URL url = ClassLoader.getSystemResource("type-annotations.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkAnnotations(schema);

        }
    }

    @Nested
    @DisplayName("Construct schema manually")
    class TestManual {

        /*
        {
            nicknames: Set<String>
            preferences: {
                diet: String,
                colour: Unresolved(Colour)
            }
            consented: Bool
        }

         */

        @Test
        @DisplayName("handles collection types")
        public void collections() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> entityTypes = new HashMap<>();
            Map<String, ActionDefinition> actions = new HashMap<>();
            Map<String, CommonTypeDefinition> commonTypes = new HashMap<>();

            RecordTypeDefinition preferences = new RecordTypeDefinition.Builder()
                    .attribute("diet", new StringType())
                    .attribute("colour", new UnresolvedTypeReference("Colour"))
                    .build();

            RecordTypeDefinition userShape = new RecordTypeDefinition.Builder()
                    .attribute("nicknames", new SetTypeDefinition(new StringType()))
                    .attribute("preferences", preferences)
                    .attribute("consented", new BooleanType())
                    .build();

            entityTypes.put("User",
                    new EntityTypeDefinition("DataCollectionApp::User", new HashSet<>(), userShape));

            Set<String> colourEnum = Set.copyOf(Arrays.asList("red", "green", "blue", "purple", "pink", "yellow"));
            entityTypes.put("Colour",
                    new EntityTypeDefinition("DataCollectionApp::Colour", new HashSet<>(),
                            new RecordTypeDefinition(), colourEnum, new HashMap<>()));

            Namespace dataCollection = new Namespace("DataCollectionApp", entityTypes, actions, commonTypes);
            schema.add(dataCollection);

            schema.accept(new SchemaResolutionVisitor());

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles circular references")
        public void circularReference() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> entityTypes = new HashMap<>();
            Map<String, ActionDefinition> actions = new HashMap<>();
            Map<String, CommonTypeDefinition> commonTypes = new HashMap<>();

            RecordTypeDefinition accountShape = new RecordTypeDefinition.Builder()
                    .attribute("friend", new UnresolvedTypeReference("Account"))
                    .build();
            entityTypes.put("Account", new EntityTypeDefinition("App::Account", new HashSet<>(), accountShape));

            Namespace app = new Namespace("App", entityTypes, actions, commonTypes);
            schema.add(app);

            schema.accept(new SchemaResolutionVisitor());

            checkCircularReference(schema);
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> oneEntities = new HashMap<>();
            oneEntities.put("A", new EntityTypeDefinition("One::A", new HashSet<>(), new RecordTypeDefinition()));

            Namespace one = new Namespace("One", oneEntities, new HashMap<>(), new HashMap<>());
            schema.add(one);

            Map<String, EntityTypeDefinition> twoEntities = new HashMap<>();

            twoEntities.put("A", new EntityTypeDefinition("Two::A", new HashSet<>(), new RecordTypeDefinition()));

            RecordTypeDefinition shape = new RecordTypeDefinition.Builder()
                    .attribute("a", new UnresolvedTypeReference("One::A"))
                    .attribute("b", new UnresolvedTypeReference("Two::A"))
                    .attribute("c", new UnresolvedTypeReference("A"))
                    .build();
            twoEntities.put("B", new EntityTypeDefinition("Two::B", new HashSet<>(), shape));

            Namespace two = new Namespace("Two", twoEntities, new HashMap<>(), new HashMap<>());
            schema.add(two);

            schema.accept(new SchemaResolutionVisitor());

            checkShadowing(schema);
        }

        @Test
        @DisplayName("resolves extension types")
        public void resolutions() {
            Schema schema = new Schema();
            Namespace namespace = new Namespace("App");
            schema.add(namespace);

            assertInstanceOf(DateTimeType.class,
                    Schema.resolveTypeReference(new UnresolvedTypeReference("datetime"), schema, namespace));
            assertInstanceOf(DecimalType.class,
                    Schema.resolveTypeReference(new UnresolvedTypeReference("decimal"), schema, namespace));
            assertInstanceOf(DurationType.class,
                    Schema.resolveTypeReference(new UnresolvedTypeReference("duration"), schema, namespace));
            assertInstanceOf(IpAddressType.class,
                    Schema.resolveTypeReference(new UnresolvedTypeReference("ipaddr"), schema, namespace));

        }

        @Test
        @DisplayName("handles malformed resolutions")
        public void badResolutions() {
            Schema schema = new Schema();
            Namespace namespace = new Namespace("App");
            schema.add(namespace);

            assertNull(Schema.resolveEntityType(null, schema, namespace));
            assertNull(Schema.resolveEntityType("Missing::Type", schema, namespace));
            assertNull(Schema.resolveCommonType(null, schema, namespace));
            assertNull(Schema.resolveCommonType("Missing::Type", schema, namespace));
            assertNull(Schema.resolveActionType(null, null, schema, namespace));
            assertNull(Schema.resolveActionType(null, "id", schema, namespace));
            assertThrows(SchemaResolutionException.class,
                    () -> Schema.resolveActionType("bad type", "id", schema, namespace));
            assertThrows(SchemaResolutionException.class,
                    () -> Schema.resolveActionType("Missing::Action", "id", schema, namespace));

            assertThrows(SchemaResolutionException.class,
                    () -> Schema.resolveTypeReference(new UnresolvedTypeReference("nonsense"), schema, namespace));

        }

        @Test
        @DisplayName("handles annotations")
        public void annotations() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> entities = new HashMap<>();

            Map<String, String> entityAnnotations = new HashMap<>();
            entityAnnotations.put("AnEntityAnnotation", "with a value!");

            EntityTypeDefinition entity = new EntityTypeDefinition("App::SomeEntity", new HashSet<>(), new RecordTypeDefinition(),
                    null,
                    entityAnnotations);
            entities.put("SomeEntity", entity);

            Map<String, ActionDefinition> actions = new HashMap<>();

            Map<String, String> actionAnnotations = new HashMap<>();
            actionAnnotations.put("AnActionAnnotation", "with a totally different value?");

            Set<String> appliesTo = Set.copyOf(List.of("App::SomeEntity"));
            actions.put("someAction",
                    new ActionDefinition("App::Action", "someAction", null, appliesTo, appliesTo, null,
                            actionAnnotations));

            Map<String, CommonTypeDefinition> types = new HashMap<>();

            types.put("SomeType", new EntityTypeReference("App::SomeType", entity));

            Namespace app = new Namespace("App", entities, actions, types);
            schema.add(app);

            schema.accept(new SchemaResolutionVisitor());

            checkAnnotations(schema);
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

        assertEquals(0, role.getShape().getAttributes().size());
        assertEquals(0, user.getShape().getAttributes().size());
        assertEquals(0, infoType.getShape().getAttributes().size());
        assertEquals(2, info.getShape().getAttributes().size());

        CommonTypeDefinition provider = info.getShape().getAttributeType("provider");
        CommonTypeDefinition patient = info.getShape().getAttributeType("patient");

        assertInstanceOf(EntityTypeReference.class, provider);
        assertInstanceOf(EntityTypeReference.class, patient);

        assertNull(provider.getName());
        assertNull(patient.getName());

        assertEquals(user, ((EntityTypeReference) provider).getDefinition());
        assertEquals(user, ((EntityTypeReference) patient).getDefinition());

        assertEquals(4, healthcareApp.actionNames().size());
        assertEquals(1, schema.actionTypes().size());
        assertEquals(4, schema.actionNames("HealthCareApp::Action").size());

        assertTrue(healthcareApp.actionNames().containsAll(
                Arrays.asList("createAppointment", "updateAppointment", "deleteAppointment", "listAppointments")));
        assertTrue(schema.actionTypes().contains("HealthCareApp::Action"));

        assertTrue(schema.actionNames("HealthCareApp::Action")
                .containsAll(Arrays.asList("createAppointment", "updateAppointment", "deleteAppointment",
                        "listAppointments")));

        ActionDefinition create = healthcareApp.getAction("createAppointment");
        ActionDefinition update = healthcareApp.getAction("updateAppointment");
        ActionDefinition delete = healthcareApp.getAction("deleteAppointment");
        ActionDefinition list = healthcareApp.getAction("listAppointments");

        assertEquals(create,
                Schema.resolveActionType("HealthCareApp::Action", "createAppointment", schema, healthcareApp));
        assertEquals(create, schema.getAction("HealthCareApp::Action", "createAppointment"));

        assertEquals("HealthCareApp::Action::\"createAppointment\"", create.getQualifiedName());
        assertEquals("HealthCareApp::Action::\"updateAppointment\"", update.getQualifiedName());
        assertEquals("HealthCareApp::Action::\"deleteAppointment\"", delete.getQualifiedName());
        assertEquals("HealthCareApp::Action::\"listAppointments\"", list.getQualifiedName());

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

        assertEquals(2, create.getAppliesToContext().getAttributes().size());
        assertNull(update.getAppliesToContext());
        assertNull(delete.getAppliesToContext());
        assertNull(list.getAppliesToContext());

        CommonTypeDefinition referrer = create.getAppliesToContext().getAttributeType("referrer");

        assertInstanceOf(EntityTypeReference.class, referrer);
        assertNull(referrer.getName());
        assertEquals(user, ((EntityTypeReference) referrer).getDefinition());

        assertEquals(2, healthcareApp.commonTypeNames().size());
        assertEquals(2, schema.commonTypeNames().size());
        assertTrue(healthcareApp.commonTypeNames().contains("AppointmentDetails"));
        assertTrue(healthcareApp.commonTypeNames().contains("Diagnosis"));
        assertTrue(schema.commonTypeNames().contains("HealthCareApp::AppointmentDetails"));
        assertTrue(schema.commonTypeNames().contains("HealthCareApp::Diagnosis"));

        CommonTypeDefinition details = healthcareApp.getCommonType("AppointmentDetails");
        assertInstanceOf(RecordTypeDefinition.class, details);

        assertEquals(details, Schema.resolveCommonType("HealthCareApp::AppointmentDetails", schema, healthcareApp));
        assertEquals(details, schema.getCommonType("HealthCareApp::AppointmentDetails"));

        CommonTypeDefinition detailAttr = create.getAppliesToContext().getAttributeType("detail");

        assertInstanceOf(CommonTypeReference.class, detailAttr);
        assertEquals(details, ((CommonTypeReference) detailAttr).getDefinition());

        CommonTypeDefinition diagnosis = healthcareApp.getCommonType("Diagnosis");
        assertInstanceOf(StringType.class, diagnosis);

        assertNull(healthcareApp.getAction("Missing"));
        assertNull(healthcareApp.getEntityType("Missing"));
        assertNull(healthcareApp.getCommonType("Missing"));
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

        CommonTypeDefinition nicknames = user.getShape().getAttributeType("nicknames");
        CommonTypeDefinition preferences = user.getShape().getAttributeType("preferences");

        assertInstanceOf(SetTypeDefinition.class, nicknames);
        assertInstanceOf(RecordTypeDefinition.class, preferences);
        assertTrue(((RecordTypeDefinition) preferences).getAttributes().keySet().stream()
                        .map(RecordTypeDefinition.Attribute::getName).toList().containsAll(Arrays.asList("diet", "colour")));
    }

    private void checkEmptyNamespace(Schema schema) {
        Namespace namespace = schema.get("");
        assertEquals(1, namespace.entityTypeNames().size());
        assertEquals(1, namespace.actionNames().size());
        assertEquals(0, namespace.commonTypeNames().size());

        EntityTypeDefinition person = schema.getEntityType("Person");
        assertNotNull(person);
        assertEquals("Person", person.getName());
        assertEquals(person, namespace.getEntityType("Person"));

        ActionDefinition slap = schema.getAction("Action", "slap");
        assertNotNull(slap);
        assertEquals("Action::\"slap\"", slap.getQualifiedName());
        assertEquals("slap", slap.getName());
        assertEquals("Action", slap.getType());
        assertEquals(slap, namespace.getAction("slap"));

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

        CommonTypeDefinition friend = account.getShape().getAttributeType("friend");
        assertNotNull(friend);

        assertInstanceOf(EntityTypeReference.class, friend);
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

        CommonTypeDefinition name = person.getShape().getAttributeType("name");
        CommonTypeDefinition age = person.getShape().getAttributeType("age");
        CommonTypeDefinition worth = person.getShape().getAttributeType("worth");
        CommonTypeDefinition bedtime = person.getShape().getAttributeType("bedtime");
        CommonTypeDefinition address = person.getShape().getAttributeType("address");
        assertNotNull(name);
        assertNotNull(age);
        assertNotNull(worth);
        assertNotNull(bedtime);
        assertNotNull(address);

        assertInstanceOf(StringType.class, name);
        assertInstanceOf(LongType.class, age);
        assertInstanceOf(DecimalType.class, worth);
        assertInstanceOf(DateTimeType.class, bedtime);
        assertInstanceOf(IpAddressType.class, address);

        CommonTypeDefinition duration = sleep.getShape().getAttributeType("duration");
        CommonTypeDefinition deep = sleep.getShape().getAttributeType("deep");
        assertNotNull(duration);
        assertNotNull(deep);

        assertInstanceOf(DurationType.class, duration);
        assertInstanceOf(BooleanType.class, deep);
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

        CommonTypeDefinition a = twoB.getShape().getAttributeType("a");
        CommonTypeDefinition b = twoB.getShape().getAttributeType("b");
        CommonTypeDefinition c = twoB.getShape().getAttributeType("c");

        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);

        assertInstanceOf(EntityTypeReference.class, a);
        assertInstanceOf(EntityTypeReference.class, b);
        assertInstanceOf(EntityTypeReference.class, c);

        assertEquals(oneA, ((EntityTypeReference) a).getDefinition());
        assertEquals(twoA, ((EntityTypeReference) b).getDefinition());
        assertEquals(twoA, ((EntityTypeReference) c).getDefinition());
    }

    private void checkAnnotations(Schema schema) {
        EntityTypeDefinition entity = schema.getEntityType("App::SomeEntity");

        assertNotNull(entity);
        assertEquals(1, entity.getAnnotations().size());
        assertEquals("with a value!", entity.getAnnotations().get("AnEntityAnnotation"));

        ActionDefinition action = schema.getAction("App::Action", "someAction");

        assertNotNull(action);
        assertEquals(1, action.getAnnotations().size());
        assertEquals("with a totally different value?", action.getAnnotations().get("AnActionAnnotation"));

        CommonTypeDefinition type = schema.getCommonType("App::SomeType");

        assertNotNull(type);
        assertInstanceOf(EntityTypeReference.class, type);
        assertEquals(entity, ((EntityTypeReference) type).getDefinition());
    }
}
