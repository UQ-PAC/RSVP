package uq.pac.rsvp.policy.datalog.invariant;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.IOException;
import java.nio.file.Path;

public class LocationTest {

    @Test
    void test() throws RsvpException, IOException {
        Path photoapp = TestUtil.getResourceDir("translation", "photoapp");
        Schema schema = Schema.parseCedarSchema(Path.of(photoapp.toString(), "photoapp.cedarschema"));
        uq.pac.rsvp.policy.ast.invariant.InvariantSet invariants = uq.pac.rsvp.policy.ast.invariant.InvariantSet.parse(Path.of(photoapp.toString(), "photoapp.invariant"));
        Invariant invariant = invariants.stream()
                .findFirst()
                .orElseThrow();
        System.out.println(invariant);
    }
}
