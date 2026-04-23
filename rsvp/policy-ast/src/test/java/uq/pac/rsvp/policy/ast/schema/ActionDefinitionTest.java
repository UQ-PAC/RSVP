package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.deserilisation.JsonParser;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

@DisplayName("Schema action AST")
public class ActionDefinitionTest {

    @Nested
    @DisplayName("Parse Cedar")
    class TestCedar {

        @Test
        @DisplayName("resolves memberOf references")
        void testMemberOf() throws RsvpException {
            URL url = ClassLoader.getSystemResource("member-of-actions.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            ActionDefinition append = schema.getAction("Action", "append");
            ActionDefinition modify = schema.getAction("Action", "modify");
            ActionDefinition edit = schema.getAction("App::Action", "edit");

            assertNotNull(append);
            assertEquals(2, append.getMemberOf().size());
            assertTrue(append.getMemberOf().contains(modify));
            assertTrue(append.getMemberOf().contains(edit));
        }

        @Test
        @DisplayName("resolves appliesTo references")
        void testAppliesTo() throws RsvpException {
            URL url = ClassLoader.getSystemResource("applies-to.cedarschema");
            Schema schema = Schema.parseCedarSchema(Path.of(url.getPath()));

            EntityTypeDefinition account = schema.getEntityType("Account");
            EntityTypeDefinition user = schema.getEntityType("User");
            EntityTypeDefinition baz = schema.getEntityType("Foo::Bar::Baz");
            assertNotNull(account);
            assertNotNull(user);
            assertNotNull(baz);

            ActionDefinition auth = schema.getAction("Action", "auth");

            assertNotNull(auth);
            assertEquals(2, auth.getAppliesToPrincipalTypes().size());
            assertTrue(auth.getAppliesToPrincipalTypes().contains(account));
            assertTrue(auth.getAppliesToPrincipalTypes().contains(user));
            assertEquals(1, auth.getAppliesToResourceTypes().size());
            assertTrue(auth.getAppliesToResourceTypes().contains(account));
            assertNull(auth.getAppliesToContext());

            ActionDefinition read = schema.getAction("Foo::Bar::Action", "read");

            assertNotNull(read);
            assertEquals(1, read.getAppliesToPrincipalTypes().size());
            assertTrue(read.getAppliesToPrincipalTypes().contains(baz));
            assertEquals(1, read.getAppliesToResourceTypes().size());
            assertTrue(read.getAppliesToResourceTypes().contains(account));
            assertNull(read.getAppliesToContext());

        }
    }

    @Nested
    @DisplayName("Parse JSON")
    class TestJSON {

        @Test
        @DisplayName("Handles nil references")
        void noReferences() throws IOException {
            URL url = ClassLoader.getSystemResource("empty-action.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            ActionDefinition action = schema.get("App").getAction("someAction");
            ActionDefinition another = schema.get("App").getAction("anotherAction");

            assertNotNull(action);
            assertNotNull(another);

            assertEquals(0, action.getMemberOf().size());
            assertEquals(0, action.getAppliesToPrincipalTypes().size());
            assertEquals(0, action.getAppliesToResourceTypes().size());
            assertEquals(0, action.getAnnotations().size());

            assertEquals(0, another.getMemberOf().size());
            assertEquals(0, another.getAppliesToPrincipalTypes().size());
            assertEquals(0, another.getAppliesToResourceTypes().size());
            assertEquals(0, another.getAnnotations().size());

            new SchemaResolutionVisitor().visitSchema(schema);

            assertEquals(0, action.getMemberOf().size());
            assertEquals(0, action.getAppliesToPrincipalTypes().size());
            assertEquals(0, action.getAppliesToResourceTypes().size());
            assertEquals(0, action.getAnnotations().size());

            assertEquals(0, another.getMemberOf().size());
            assertEquals(0, another.getAppliesToPrincipalTypes().size());
            assertEquals(0, another.getAppliesToResourceTypes().size());
            assertEquals(0, another.getAnnotations().size());
        }

        @Test
        @DisplayName("Resolves references")
        void resolvesMemberOf() throws IOException {
            URL url = ClassLoader.getSystemResource("action.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            new SchemaResolutionVisitor().visitSchema(schema);

            ActionDefinition action = schema.getAction("Local::Action", "anotherAction");

            assertNotNull(action);

            assertEquals(1, action.getMemberOf().size());
            assertTrue(action.getMemberOf().contains(schema.getAction("App::Action", "someAction")));
            assertEquals(2, action.getAppliesToPrincipalTypes().size());
            assertTrue(action.getAppliesToPrincipalTypes().contains(schema.getEntityType("App::User")));
            assertTrue(action.getAppliesToPrincipalTypes().contains(schema.getEntityType("Local::Loser")));
            assertEquals(1, action.getAppliesToResourceTypes().size());
            assertTrue(action.getAppliesToResourceTypes().contains(schema.getEntityType("Local::Loser")));
        }

        @Test
        @DisplayName("Resolves references (no namespaces)")
        void resolvesMemberOfNNS() throws IOException {
            URL url = ClassLoader.getSystemResource("action.nns.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            new SchemaResolutionVisitor().visitSchema(schema);

            ActionDefinition action = schema.getAction("Action", "anotherAction");

            assertNotNull(action);

            assertEquals(1, action.getMemberOf().size());
            assertTrue(action.getMemberOf().contains(schema.getAction("Action", "someAction")));
            assertEquals(2, action.getAppliesToPrincipalTypes().size());
            assertTrue(action.getAppliesToPrincipalTypes().contains(schema.getEntityType("User")));
            assertTrue(action.getAppliesToPrincipalTypes().contains(schema.getEntityType("Loser")));
            assertEquals(1, action.getAppliesToResourceTypes().size());
            assertTrue(action.getAppliesToResourceTypes().contains(schema.getEntityType("Loser")));
        }

    }

    @Nested
    @DisplayName("Construct actions manually")
    class TestManual {

        private static Schema schema;
        private static Namespace local;

        @BeforeAll
        static void beforeAll() {
            schema = new Schema();

            Map<String, EntityTypeDefinition> localEntities = new HashMap<>();
            localEntities.put("Loser", new EntityTypeDefinition("Local::Loser"));

            local = new Namespace("Local", localEntities, null, null);

            Map<String, EntityTypeDefinition> otherEntities = new HashMap<>();
            otherEntities.put("User", new EntityTypeDefinition("App::User"));

            Map<String, ActionDefinition> otherActions = new HashMap<>();
            otherActions.put("someAction", new ActionDefinition("App::Action", "someAction"));

            Namespace other = new Namespace("App", otherEntities, otherActions, null);

            schema.add(other);
            schema.add(local);
        }

        @Test
        @DisplayName("Handles nil references")
        void noReferences() {

            ActionDefinition action = new ActionDefinition();

            action.resolveReferences(schema, local);

            assertNull(action.getName());
            assertEquals(0, action.getMemberOf().size());
            assertEquals(0, action.getAppliesToPrincipalTypes().size());
            assertEquals(0, action.getAppliesToResourceTypes().size());
            assertEquals(0, action.getAnnotations().size());
        }

        @Test
        @DisplayName("Resolves references")
        void resolvesReferences() {
            Set<ActionDefinition.ActionReference> memberOf = new HashSet<>();

            memberOf.add(new ActionDefinition.ActionReference("someAction", "App::Action"));

            Set<String> principalTypes = Set.copyOf(Arrays.asList("App::User", "Loser"));
            Set<String> resourceTypes = Set.copyOf(Arrays.asList("Loser"));

            ActionDefinition action = new ActionDefinition("", "", memberOf, principalTypes, resourceTypes, null, null);

            action.resolveReferences(schema, local);

            assertEquals(1, action.getMemberOf().size());
            assertTrue(action.getMemberOf().contains(schema.get("App").getAction("someAction")));
            assertEquals(2, action.getAppliesToPrincipalTypes().size());
            assertTrue(action.getAppliesToPrincipalTypes().contains(schema.get("App").getEntityType("User")));
            assertTrue(action.getAppliesToPrincipalTypes().contains(schema.get("Local").getEntityType("Loser")));
            assertEquals(1, action.getAppliesToResourceTypes().size());
            assertTrue(action.getAppliesToResourceTypes().contains(schema.get("Local").getEntityType("Loser")));
        }

        @Test
        @DisplayName("Handles invalid references")
        void unresolvedReferences() {
            Set<ActionDefinition.ActionReference> memberOf = new HashSet<>();

            memberOf.add(new ActionDefinition.ActionReference("missingAction", "App::Action"));

            Set<String> principalTypes = Set.copyOf(Arrays.asList("App::Loser", "Missing"));
            Set<String> resourceTypes = Set.copyOf(Arrays.asList("Missing"));

            ActionDefinition action = new ActionDefinition("", "", memberOf, principalTypes, resourceTypes, null, null);

            assertThrows(SchemaResolutionException.class, () -> action.resolveReferences(schema, local));

            assertEquals(0, action.getMemberOf().size());
        }

        @Test
        @DisplayName("Handles annotations")
        void annotations() {

            Map<String, String> annotations = new HashMap<>();
            annotations.put("some_annotation", "for testing");
            annotations.put("another_annotation", "for luck");

            ActionDefinition action = new ActionDefinition("", "", null, null, null, null, annotations);

            assertEquals(2, action.getAnnotations().size());
            assertEquals("for testing", action.getAnnotations().get("some_annotation"));
            assertEquals("for luck", action.getAnnotations().get("another_annotation"));

        }
    }
}
