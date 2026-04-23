package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.deserilisation.SchemaJsonParser;
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

        @Test
        @DisplayName("handles aliases")
        void testAliases() throws RsvpException {
            Schema schema = Schema.parseCedarSchema("entity File = {\n" + //
                    "    path: Path,\n" + //
                    "    access: FileAttr,\n" + //
                    "    permission: FilePermission\n" + //
                    "};\n" + //
                    "\n" + //
                    "entity Directory = {\n" + //
                    "    access: DirAttr,\n" + //
                    "    path: Path,\n" + //
                    "    permission: DirPermission\n" + //
                    "};\n" + //
                    "\n" + //
                    "type FileAttr = String;\n" + //
                    "type DirAttr = String;\n" + //
                    "type FilePermission = {\n" + //
                    "    read: Bool,\n" + //
                    "    write: Bool,\n" + //
                    "    exec: Bool\n" + //
                    "};\n" + //
                    "\n" + //
                    "type DirPermission = {\n" + //
                    "    read: Bool,\n" + //
                    "    write: Bool,\n" + //
                    "    exec: Bool\n" + //
                    "};\n" + //
                    "\n" + //
                    "type Path = String;\n" + //
                    "\n" + //
                    "action \"introspect\" appliesTo {\n" + //
                    "    principal: [File, Directory],\n" + //
                    "    resource: [File, Directory]\n" + //
                    "};");

            EntityTypeDefinition file = schema.getEntityType("File");
            EntityTypeDefinition directory = schema.getEntityType("Directory");
            assertNotNull(file);
            assertNotNull(directory);

            CommonTypeDefinition fileAttr = schema.getCommonType("FileAttr");
            CommonTypeDefinition dirAttr = schema.getCommonType("DirAttr");
            CommonTypeDefinition filePermission = schema.getCommonType("FilePermission");
            CommonTypeDefinition dirPermission = schema.getCommonType("DirPermission");
            CommonTypeDefinition path = schema.getCommonType("Path");
            assertNotNull(fileAttr);
            assertNotNull(dirAttr);
            assertNotNull(filePermission);
            assertNotNull(dirPermission);
            assertNotNull(path);
            assertTrue(fileAttr instanceof StringType);
            assertTrue(dirAttr instanceof StringType);
            assertTrue(filePermission instanceof RecordTypeDefinition);
            assertTrue(dirPermission instanceof RecordTypeDefinition);
            assertTrue(path instanceof StringType);

            CommonTypeDefinition filePathAttr = file.getShape().getAttributeType("path");
            assertNotNull(filePathAttr);
            assertTrue(filePathAttr instanceof CommonTypeReference);
            assertEquals(path, ((CommonTypeReference) filePathAttr).getDefinition());

            CommonTypeDefinition fileAccessAttr = file.getShape().getAttributeType("access");
            assertNotNull(fileAccessAttr);
            assertTrue(fileAccessAttr instanceof CommonTypeReference);
            assertEquals(fileAttr, ((CommonTypeReference) fileAccessAttr).getDefinition());

            CommonTypeDefinition filePermissionAttr = file.getShape().getAttributeType("permission");
            assertNotNull(filePermissionAttr);
            assertTrue(filePermissionAttr instanceof CommonTypeReference);
            assertEquals(filePermission, ((CommonTypeReference) filePermissionAttr).getDefinition());

            CommonTypeDefinition dirPathAttr = directory.getShape().getAttributeType("path");
            assertNotNull(dirPathAttr);
            assertTrue(dirPathAttr instanceof CommonTypeReference);
            System.err.println(((CommonTypeReference) dirPathAttr).getDefinition().toString());
            assertEquals(path, ((CommonTypeReference) dirPathAttr).getDefinition());

            CommonTypeDefinition dirAccessAttr = directory.getShape().getAttributeType("access");
            assertNotNull(dirAccessAttr);
            assertTrue(dirAccessAttr instanceof CommonTypeReference);
            assertEquals(dirAttr, ((CommonTypeReference) dirAccessAttr).getDefinition());

            CommonTypeDefinition dirPermissionAttr = directory.getShape().getAttributeType("permission");
            assertNotNull(dirPermissionAttr);
            assertTrue(dirPermissionAttr instanceof CommonTypeReference);
            assertEquals(dirPermission, ((CommonTypeReference) dirPermissionAttr).getDefinition());

            CommonTypeDefinition filePermRead = ((RecordTypeDefinition) filePermission).getAttributeType("read");
            assertNotNull(filePermRead);
            assertTrue(filePermRead instanceof BooleanType);

            CommonTypeDefinition filePermWrite = ((RecordTypeDefinition) filePermission).getAttributeType("write");
            assertNotNull(filePermWrite);
            assertTrue(filePermWrite instanceof BooleanType);

            CommonTypeDefinition filePermExec = ((RecordTypeDefinition) filePermission).getAttributeType("exec");
            assertNotNull(filePermExec);
            assertTrue(filePermExec instanceof BooleanType);

            CommonTypeDefinition dirPermRead = ((RecordTypeDefinition) dirPermission).getAttributeType("read");
            assertNotNull(dirPermRead);
            assertTrue(dirPermRead instanceof BooleanType);

            CommonTypeDefinition dirPermWrite = ((RecordTypeDefinition) dirPermission).getAttributeType("write");
            assertNotNull(dirPermWrite);
            assertTrue(dirPermWrite instanceof BooleanType);

            CommonTypeDefinition dirPermExec = ((RecordTypeDefinition) dirPermission).getAttributeType("exec");
            assertNotNull(dirPermExec);
            assertTrue(dirPermExec instanceof BooleanType);

            ActionDefinition introspect = schema.getAction("Action", "introspect");
            assertNotNull(introspect);

            Set<EntityTypeDefinition> principals = introspect.getAppliesToPrincipalTypes();
            Set<EntityTypeDefinition> resources = introspect.getAppliesToResourceTypes();

            assertEquals(2, principals.size());
            assertEquals(2, resources.size());

            assertTrue(principals.containsAll(Arrays.asList(file, directory)));
            assertTrue(resources.containsAll(Arrays.asList(file, directory)));

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
            Schema schema = SchemaJsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkHealthCareSchema(schema);
        }

        @Test
        @DisplayName("handles collection types")
        public void collections() throws IOException {
            URL url = ClassLoader.getSystemResource("collection-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = SchemaJsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkCollectionSchema(schema);
        }

        @Test
        @DisplayName("handles reserved types")
        public void reservedTypes()
                throws IOException {
            URL url = ClassLoader.getSystemResource("reserved-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = SchemaJsonParser.parseSchema(json);

            checkReservedTypes(schema);
        }

        @Test
        @DisplayName("handles unnamed namespace")
        public void emptyNamespace() throws IOException {
            URL url = ClassLoader.getSystemResource("unnamed-namespace.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = SchemaJsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkEmptyNamespace(schema);
        }

        @Test
        @DisplayName("handles legal shadowing")
        public void legalShadow() throws IOException {
            URL url = ClassLoader.getSystemResource("legal-shadow.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = SchemaJsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkShadowing(schema);
        }

        @Test
        @DisplayName("handles resolved type format")
        public void invalidTypes() throws IOException {
            URL url = ClassLoader.getSystemResource("invalid-types.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = SchemaJsonParser.parseSchema(json);

            assertThrows(SchemaResolutionException.class, () -> schema.accept(new SchemaResolutionVisitor()));
        }

        @Test
        @DisplayName("handles annotations")
        public void annotations() throws IOException {
            URL url = ClassLoader.getSystemResource("type-annotations.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = SchemaJsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            checkAnnotations(schema);

        }

        @Test
        @DisplayName("handles string input")
        void test() throws RsvpException {
            Schema schema = Schema.parseCedarSchema("type Task = {\n" + //
                    "    \"id\": Long,\n" + //
                    "    \"name\": String,\n" + //
                    "    \"state\": String,\n" + //
                    "};\n" + //
                    "\n" + //
                    "type Tasks = Set<Task>;\n" + //
                    "entity List in [Application] = {\n" + //
                    "  \"editors\": Team,\n" + //
                    "  \"name\": String,\n" + //
                    "  \"owner\": User,\n" + //
                    "  \"readers\": Team,\n" + //
                    "  \"tasks\": Tasks,\n" + //
                    "};\n" + //
                    "entity Application enum [\"TinyTodo\"];\n" + //
                    "entity User in [Team, Application] = {\n" + //
                    "  \"joblevel\": Long,\n" + //
                    "  \"location\": String,\n" + //
                    "};\n" + //
                    "entity Team in [Team, Application];\n" + //
                    "\n" + //
                    "action DeleteList, GetList, UpdateList appliesTo {\n" + //
                    "  principal: [User],\n" + //
                    "  resource: [List]\n" + //
                    "};\n" + //
                    "action CreateList, GetLists appliesTo {\n" + //
                    "  principal: [User],\n" + //
                    "  resource: [Application]\n" + //
                    "};\n" + //
                    "action CreateTask, UpdateTask, DeleteTask appliesTo {\n" + //
                    "  principal: [User],\n" + //
                    "  resource: [List]\n" + //
                    "};\n" + //
                    "action EditShare appliesTo {\n" + //
                    "  principal: [User],\n" + //
                    "  resource: [List]\n" + //
                    "};");

            CommonTypeDefinition task = schema.getCommonType("Task");
            CommonTypeDefinition tasks = schema.getCommonType("Tasks");
            assertNotNull(task);
            assertNotNull(tasks);

            assertTrue(tasks instanceof SetTypeDefinition);

            CommonTypeDefinition tasksElem = ((SetTypeDefinition) tasks).getElementType();
            assertNotNull(tasksElem);
            assertTrue(tasksElem instanceof CommonTypeReference);
            assertEquals(task, ((CommonTypeReference) tasksElem).getDefinition());

            EntityTypeDefinition list = schema.getEntityType("List");
            assertNotNull(list);

            CommonTypeDefinition tasksProp = list.getShapeAttributeType("tasks");
            assertNotNull(tasksProp);
            assertTrue(tasksProp instanceof CommonTypeReference);
            assertEquals(tasks, ((CommonTypeReference) tasksProp).getDefinition());
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
            entityTypes.put("User", new EntityTypeDefinition("DataCollectionApp::User", new HashSet<>(), userShape));

            Set<String> colourEnum = Set.copyOf(Arrays.asList("red", "green", "blue", "purple", "pink", "yellow"));
            entityTypes.put("Colour",
                    new EntityTypeDefinition("DataCollectionApp::Colour", new HashSet<>(), new HashMap<>(),
                            colourEnum, new HashMap<>()));

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

            Map<String, CommonTypeDefinition> accountShape = new HashMap<>();

            accountShape.put("friend", new UnresolvedTypeReference("Account"));
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
            oneEntities.put("A", new EntityTypeDefinition("One::A", new HashSet<>(), new HashMap<>()));

            Namespace one = new Namespace("One", oneEntities, new HashMap<>(), new HashMap<>());
            schema.add(one);

            Map<String, EntityTypeDefinition> twoEntities = new HashMap<>();

            twoEntities.put("A", new EntityTypeDefinition("Two::A", new HashSet<>(), new HashMap<>()));

            Map<String, CommonTypeDefinition> shape = new HashMap<>();

            shape.put("a", new UnresolvedTypeReference("One::A"));
            shape.put("b", new UnresolvedTypeReference("Two::A"));
            shape.put("c", new UnresolvedTypeReference("A"));
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

            assertTrue(Schema.resolveTypeReference(new UnresolvedTypeReference("datetime"), schema,
                    namespace) instanceof DateTimeType);
            assertTrue(Schema.resolveTypeReference(new UnresolvedTypeReference("decimal"), schema,
                    namespace) instanceof DecimalType);
            assertTrue(Schema.resolveTypeReference(new UnresolvedTypeReference("duration"), schema,
                    namespace) instanceof DurationType);
            assertTrue(Schema.resolveTypeReference(new UnresolvedTypeReference("ipaddr"), schema,
                    namespace) instanceof IpAddressType);

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

            EntityTypeDefinition entity = new EntityTypeDefinition("App::SomeEntity", new HashSet<>(), new HashMap<>(),
                    null,
                    entityAnnotations);
            entities.put("SomeEntity", entity);

            Map<String, ActionDefinition> actions = new HashMap<>();

            Map<String, String> actionAnnotations = new HashMap<>();
            actionAnnotations.put("AnActionAnnotation", "with a totally different value?");

            Set<String> appliesTo = Set.copyOf(Arrays.asList("App::SomeEntity"));
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

        assertEquals(0, role.getShapeAttributeNames().size());
        assertEquals(0, user.getShapeAttributeNames().size());
        assertEquals(0, infoType.getShapeAttributeNames().size());
        assertEquals(2, info.getShapeAttributeNames().size());

        CommonTypeDefinition provider = info.getShapeAttributeType("provider");
        CommonTypeDefinition patient = info.getShapeAttributeType("patient");

        assertTrue(provider instanceof EntityTypeReference);
        assertTrue(patient instanceof EntityTypeReference);

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

        assertEquals(2, create.getAppliesToContext().getAttributeNames().size());
        assertNull(update.getAppliesToContext());
        assertNull(delete.getAppliesToContext());
        assertNull(list.getAppliesToContext());

        CommonTypeDefinition referrer = create.getAppliesToContext().getAttributeType("referrer");

        assertTrue(referrer instanceof EntityTypeReference);
        assertNull(referrer.getName());
        assertEquals(user, ((EntityTypeReference) referrer).getDefinition());

        assertEquals(2, healthcareApp.commonTypeNames().size());
        assertEquals(2, schema.commonTypeNames().size());
        assertTrue(healthcareApp.commonTypeNames().contains("AppointmentDetails"));
        assertTrue(healthcareApp.commonTypeNames().contains("Diagnosis"));
        assertTrue(schema.commonTypeNames().contains("HealthCareApp::AppointmentDetails"));
        assertTrue(schema.commonTypeNames().contains("HealthCareApp::Diagnosis"));

        CommonTypeDefinition details = healthcareApp.getCommonType("AppointmentDetails");
        assertTrue(details instanceof RecordTypeDefinition);

        assertEquals(details, Schema.resolveCommonType("HealthCareApp::AppointmentDetails", schema, healthcareApp));
        assertEquals(details, schema.getCommonType("HealthCareApp::AppointmentDetails"));

        CommonTypeDefinition detailAttr = create.getAppliesToContext().getAttributeType("detail");

        assertTrue(detailAttr instanceof CommonTypeReference);
        assertEquals(details, ((CommonTypeReference) detailAttr).getDefinition());

        CommonTypeDefinition diagnosis = healthcareApp.getCommonType("Diagnosis");
        assertTrue(diagnosis instanceof StringType);

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
        assertTrue(type instanceof EntityTypeReference);
        assertEquals(entity, ((EntityTypeReference) type).getDefinition());
    }
}
