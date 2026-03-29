package uq.pac.rsvp.policy.datalog.invariant;

import com.cedarpolicy.model.entity.Entities;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.io.IOException;
import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

public class TypingTest {
    Logger logger = new Logger();

    private final Schema schema;
    private final Entities entities;

    public TypingTest() throws RsvpException, IOException {
        Path schemaPath = TestUtil.getResourceDir("translation", "photoapp", "photoapp.cedarschema");
        this.schema = Schema.parseCedarSchema(schemaPath);
        Path entitiesPath = TestUtil.getResourceDir("translation", "photoapp", "entities.json");
        this.entities = Translation.updateEntities(Entities.parse(entitiesPath), schema);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "true",
            "false",
            "false && true",
            "false || true",
            "!false || true",
            "!(false || true)",
            "!(false || true)",
            "1 == 2",
            "1 >= 2",
            "5 > 4",
            "1 + 2 == 3",
            "\"a\" != \"b\"",
            "principal == Account::\"Alice\" for all principal: Account",
            "principal == resource for all principal: Account, resource: Account",
            "principal.role == resource.role for all principal: Account, resource: Account",
            "action == Action::\"createAlbum\" for all action: Action",

            // +, -, * are only over numeric types
            "alice.age == (bob.age - 1)  for all alice: Account, bob: Account",
    })
    void ok(String invariantText) {
        String text = """
                @invariant("test")
                        %s;
                """.formatted(invariantText);
        logger.info(YELLOW, "Parsing invariant: " + invariantText);
        Invariant invariant = InvariantSet.parse(text).getInvariant("test");
        new InvariantValidation (schema, entities, invariant).validate(invariant);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Standalone expressions are expected to evaluate to booleans
            "1",
            "1 + 2",
            "-(1 + 2)",
            "\"foo\"",
            "principal.age for all principal: Account",
            "Account::\"Alice\"",

            // Invalid entity name
            "principal == Accounts::\"Alice\" for all principal: Account",
            "principal == Account::\"Alic\" for all principal: Account",

            // Invalid action name
            "action == Action::\"createAlbu\" for all action: Action",
            "action == Actions::\"createAlbum\" for all action: Action",

            // Arithmetic negation is only over numeric types
            "-action == Actions::\"createAlbum\" for all action: Action",
            // Logical negation is only over boolean types
            "!action == Actions::\"createAlbum\" for all action: Action",

            // +, -, * are only over numeric types
            "alice.age == (bob.age - 1)  for all alice: Account, bob: Account",

    })
    void fail(String text) {
        try {
            ok(text);
        } catch (InvariantValidation.Error error) {
            logger.info(MAGENTA, "    Expected error: " + error.getMessage());
        }
    }
}
