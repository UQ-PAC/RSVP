package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.value.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.policy.datalog.TestUtil;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EntitySetTest {

    private final static Path TESTDIR = Path.of(TestUtil.RESOURCEDIR.toString(), "entity");

    @ParameterizedTest
    @ValueSource(strings = {
            "entities-tab",
            "entities-undefined"
    })
    void unsupportedName(String name) {
        Path entity = TestUtil.findFile(TESTDIR, name + ".json"),
                schema = TestUtil.findFile(TESTDIR, ".cedarschema"),
                policy = TestUtil.findFile(TESTDIR, ".cedar"),
                invariants = TestUtil.findFile(TESTDIR, ".invariant");
        assertThrows(TranslationError.class, () -> Translation.validate(schema, policy, entity, invariants));
    }

    @ParameterizedTest
    @ValueSource(strings = { "entities" })
    void supported(String name) {
        Path entity = TestUtil.findFile(TESTDIR, name + ".json"),
                schema = TestUtil.findFile(TESTDIR, ".cedarschema"),
                policy = TestUtil.findFile(TESTDIR, ".cedar"),
                invariants = TestUtil.findFile(TESTDIR, ".invariant");
        assertDoesNotThrow(() -> Translation.validate(schema, policy, entity, invariants));
    }

    // Take all the entities from the translation sets and compare them with what Cedar gets
    @Test
    void buildTest() throws IOException {
        Path path = TestUtil.getResourceDir("translation");
        for (Path ep : TestUtil.findFiles(path, ".json")) {
            Entities cedarEntities = Entities.parse(ep);
            List<Entity> rsvpEntities = EntitySet.parse(ep).stream().toList();
            assertEquals(cedarEntities.getEntities().size(), rsvpEntities.size());

            Map<String, Entity> rsvpEntityMap = new HashMap<>();
            rsvpEntities.forEach(e -> rsvpEntityMap.put(e.getEuid().getReference(), e));

            Map<String, com.cedarpolicy.model.entity.Entity> cedarEntityMap = new HashMap<>();
            cedarEntities.getEntities().forEach(e -> cedarEntityMap.put(e.getEUID().toCedarExpr(), e));

            assertEquals(cedarEntityMap.size(), rsvpEntityMap.size());

            for (String uid : rsvpEntityMap.keySet()) {
                Entity r = rsvpEntityMap.get(uid);
                com.cedarpolicy.model.entity.Entity c = cedarEntityMap.get(uid);

                assertNotNull(r);
                assertNotNull(c);

                assertEquals(r.getEuid(), cedarToRsvp(c.getEUID()));
                assertEquals(r.getAttrs(), cedarToRsvp(new CedarMap(c.attrs)));
                assertEquals(r.getParents(), c.parentsEUIDs.stream().map(this::cedarToRsvp)
                        .map(e -> (EntityReference) e)
                        .collect(Collectors.toSet()));
            }
        }
    }

    // Convert Cedar Value to RSVP entity one for comparison
    EntityValue cedarToRsvp(Value value) {
        return switch (value) {
            case PrimBool b -> new BooleanValue(b.getValue());
            case PrimLong l -> new LongValue(l.getValue());
            case PrimString s -> new StringValue(s.toString());
            case EntityUID ref -> new EntityReference(ref.getType().toString(), ref.getId().toString());
            case CedarList lst -> new SetValue(lst.stream().map(this::cedarToRsvp).collect(Collectors.toSet()));
            case CedarMap map ->
                new RecordValue(map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> cedarToRsvp(e.getValue()))));
            default -> throw new RuntimeException("Unsupported value: " + value);
        };
    }
}
