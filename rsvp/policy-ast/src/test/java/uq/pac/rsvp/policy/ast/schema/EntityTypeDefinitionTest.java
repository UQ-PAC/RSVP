package uq.pac.rsvp.policy.ast.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uq.pac.rsvp.policy.ast.deserilisation.JsonParser;
import uq.pac.rsvp.policy.ast.visitor.SchemaResolutionVisitor;

@DisplayName("Schema entity type AST")
public class EntityTypeDefinitionTest {
    @Nested
    @DisplayName("Parse JSON")
    class TestJSON {

        @Test
        @DisplayName("Handles empty entity")
        void empty() throws IOException {
            URL url = ClassLoader.getSystemResource("empty-entity.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            EntityTypeDefinition some = schema.getEntityType("App::SomeEntity");
            EntityTypeDefinition another = schema.getEntityType("App::AnotherEntity");

            assertEquals(0, some.getEntityNamesEnum().size());
            assertEquals(0, some.getMemberOfTypes().size());
            assertEquals(0, some.getShapeAttributeNames().size());
            assertEquals(0, some.getAnnotations().size());

            assertEquals(0, another.getEntityNamesEnum().size());
            assertEquals(0, another.getMemberOfTypes().size());
            assertEquals(0, another.getShapeAttributeNames().size());
            assertEquals(0, another.getAnnotations().size());
        }

        @Test
        @DisplayName("Resolves memberOf types")
        void memberOf() throws IOException {
            URL url = ClassLoader.getSystemResource("entity-refs.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            schema.accept(new SchemaResolutionVisitor());

            EntityTypeDefinition item = schema.getEntityType("App::Item");
            EntityTypeDefinition user = schema.getEntityType("App::User");
            EntityTypeDefinition role = schema.getEntityType("Lib::Role");

            assertEquals(2, user.getMemberOfTypes().size());

            assertTrue(user.getMemberOfTypes().contains(role));
            assertTrue(user.getMemberOfTypes().contains(item));
        }

        @Test
        @DisplayName("Handles missing types")
        void unresolved() throws IOException {
            URL url = ClassLoader.getSystemResource("missing-refs.cedarschema.json");
            String json = Files.readString(Path.of(url.getPath()));
            Schema schema = JsonParser.parseSchema(json);

            assertThrows(SchemaResolutionException.class, () -> schema.accept(new SchemaResolutionVisitor()));
        }

    }

    @Nested
    @DisplayName("Construct types manually")
    class TestManual {

        @Test
        @DisplayName("Handles empty entity")
        void empty() {

            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> entities = new HashMap<>();
            entities.put("SomeEntity", new EntityTypeDefinition("App::SomeEntity"));
            entities.put("AnotherEntity",
                    new EntityTypeDefinition("App::AnotherEntity", Collections.emptySet(), Collections.emptyMap(),
                            Collections.emptySet(), Collections.emptyMap()));

            Namespace app = new Namespace("App", entities, null);
            schema.add(app);

            schema.accept(new SchemaResolutionVisitor());

            EntityTypeDefinition some = schema.getEntityType("App::SomeEntity");
            EntityTypeDefinition another = schema.getEntityType("App::AnotherEntity");

            assertNotNull(some);
            assertNotNull(another);

            assertEquals(0, some.getEntityNamesEnum().size());
            assertEquals(0, some.getMemberOfTypes().size());
            assertEquals(0, some.getShapeAttributeNames().size());
            assertEquals(0, some.getAnnotations().size());

            assertEquals(0, another.getEntityNamesEnum().size());
            assertEquals(0, another.getMemberOfTypes().size());
            assertEquals(0, another.getShapeAttributeNames().size());
            assertEquals(0, another.getAnnotations().size());
        }

        @Test
        @DisplayName("Resolves memberOf types")
        void memberOf() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> appEntities = new HashMap<>();
            appEntities.put("Item", new EntityTypeDefinition("App::Item"));
            appEntities.put("User",
                    new EntityTypeDefinition("App::User", Set.of("Lib::Role", "Item"), Collections.emptyMap(),
                            Collections.emptySet()));

            Namespace app = new Namespace("App", appEntities, null);
            schema.add(app);

            Map<String, EntityTypeDefinition> libEntities = new HashMap<>();
            libEntities.put("Role", new EntityTypeDefinition("Lib::Role"));

            Namespace lib = new Namespace("Lib", libEntities, null);
            schema.add(lib);

            assertEquals(0, app.getEntityType("User").getMemberOfTypes().size());

            schema.accept(new SchemaResolutionVisitor());

            EntityTypeDefinition item = schema.getEntityType("App::Item");
            EntityTypeDefinition user = schema.getEntityType("App::User");
            EntityTypeDefinition role = schema.getEntityType("Lib::Role");

            assertNotNull(item);
            assertNotNull(user);
            assertNotNull(role);
            assertEquals(2, user.getMemberOfTypes().size());

            assertTrue(user.getMemberOfTypes().contains(role));
            assertTrue(user.getMemberOfTypes().contains(item));

        }

        @Test
        @DisplayName("Handles missing types")
        void unresolved() {
            Schema schema = new Schema();

            Map<String, EntityTypeDefinition> appEntities = new HashMap<>();
            appEntities.put("Loser",
                    new EntityTypeDefinition("App::Loser", Set.of("Lib::Role", "Lib::Invalid"), Collections.emptyMap(),
                            Collections.emptySet()));

            Namespace app = new Namespace("App", appEntities, null);
            schema.add(app);

            Map<String, EntityTypeDefinition> libEntities = new HashMap<>();
            libEntities.put("Role", new EntityTypeDefinition("Lib::Role"));

            Namespace lib = new Namespace("Lib", libEntities, null);
            schema.add(lib);

            assertThrows(SchemaResolutionException.class, () -> schema.accept(new SchemaResolutionVisitor()));
        }

        @Test
        @DisplayName("Handles enum values")
        void enums() {
            EntityTypeDefinition empty = new EntityTypeDefinition();
            assertEquals(0, empty.getEntityNamesEnum().size());

            EntityTypeDefinition enums = new EntityTypeDefinition("", null, null, Set.of("One", "Two", "Three"));
            assertEquals(3, enums.getEntityNamesEnum().size());

            assertTrue(enums.getEntityNamesEnum().containsAll(Arrays.asList("One", "Two", "Three")));

        }
    }
}
