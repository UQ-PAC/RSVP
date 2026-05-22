package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.value.*;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
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

    @TestFactory
    List<DynamicTest> differentialTest() {
        Path path = TestUtil.getResourceDir("translation");
        return TestUtil.findFiles(path, ".json").stream()
                .map(p -> DynamicTest.dynamicTest(p.getParent().getFileName().toString(), () -> {
                    buildTest(p);
                }))
                .toList();
    }

    // Take all the entities from the translation sets and compare them with what Cedar gets
    void buildTest(Path path) throws IOException, IllegalAccessException {
        Entities cedarEntities = Entities.parse(path);
        List<Entity> rsvpEntities = EntitySet.parse(path).stream().toList();
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

    // Convert Cedar Value to RSVP entity one for comparison
    EntityValue cedarToRsvp(Value value) {
        return switch (value) {
            case PrimBool b -> new BooleanValue(b.getValue());
            case PrimLong l -> new LongValue(l.getValue());
            case PrimString s -> new StringValue(StringEscapeUtils.escapeJava(s.toString()));
            case EntityUID ref -> new EntityReference(
                    StringEscapeUtils.escapeJava(ref.getType().toString()),
                    StringEscapeUtils.escapeJava(ref.getId().toString()));
            case CedarList lst -> new SetValue(lst.stream().map(this::cedarToRsvp).collect(Collectors.toSet()));
            case CedarMap map ->
                new RecordValue(map.entrySet().stream()
                        .collect(Collectors.toMap(e -> new AttributeName(e.getKey()), e -> cedarToRsvp(e.getValue()))));
            default -> throw new RuntimeException("Unsupported value: " + value);
        };
    }
}
