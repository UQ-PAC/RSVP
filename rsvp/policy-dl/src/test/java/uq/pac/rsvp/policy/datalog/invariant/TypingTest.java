package uq.pac.rsvp.policy.datalog.invariant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;
import uq.pac.rsvp.policy.datalog.util.Logger;

import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

public class TypingTest {
    Logger logger = new Logger();

    private final Schema schema;

    public TypingTest() throws RsvpException {
        Path path = TestUtil.getResourceDir("translation", "photoapp", "photoapp.cedarschema");
        this.schema = Schema.parseCedarSchema(path);
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
            "principal == resource for all principal: Account, resource: Account",
            "principal.role == resource.role for all principal: Account, resource: Account",

            "true"
    })
    void ok(String invariantText) {
        String text = """
                @invariant("test")
                        %s;
                """.formatted(invariantText);
        logger.info(YELLOW, "Parsing invariant: " + invariantText);
        Invariant invariant = InvariantSet.parse(text).getInvariant("test");
        new InvariantValidation (schema, invariant).validate(invariant);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Standalone expressions are expected to evaluate to booleans
            "1",
            "1 + 2",
            "-(1 + 2)",
            "\"foo\"",
            "principal.age for all principal: Account",
            "Account::\"Alice\""
    })
    void fail(String text) {
        try {
            ok(text);
        } catch (InvariantValidation.Error error) {
            logger.info(MAGENTA, "    Expected error: " + error.getMessage());
        }
    }
}
