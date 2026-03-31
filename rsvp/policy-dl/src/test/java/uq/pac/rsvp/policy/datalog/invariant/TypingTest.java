package uq.pac.rsvp.policy.datalog.invariant;

import com.cedarpolicy.model.entity.Entities;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.TestUtil;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;
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

            // Equality, inequality over Long, String, Bool or Entity is ok
            "alice == bob for all alice: Account, bob: Account",
            "alice == bob for all alice: Action, bob: Action",
            "alice.age == bob.age for all alice: Account, bob: Account",
            "alice.age == bob.age for all alice: Account, bob: Account",
            "one.code == another.code for all one: PhotoPermission, another: PhotoPermission",
            "one.permission.read == another.permission.write for all one: PhotoPermission, another: PhotoPermission",

    })
    void ok(String invariantText) {
        String text = """
                @invariant("test")
                        %s;
                """.formatted(invariantText);
        logger.info(YELLOW, "Parsing invariant: " + invariantText);
        Invariant invariant = InvariantSet.parse(text).stream().findAny().orElseThrow();
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

            // Invalid entity name (type reference)
            "principal == Accounts::\"Alice\" for all principal: Account",
            "principal == Account::\"Alic\" for all principal: Account",

            // Invalid action name (type reference)
            "action == Action::\"createAlbu\" for all action: Action",
            "action == Actions::\"createAlbum\" for all action: Action",

            // Invalid property access
            "one.codes == another.code for all one: PhotoPermission, another: PhotoPermission",
            "one.permissions.read == another.permission.read for all one: PhotoPermission, another: PhotoPermission",
            "one.permission.reads == another.permission.read for all one: PhotoPermission, another: PhotoPermission",

            // Arithmetic negation is only over numeric types
            "-action == Actions::\"createAlbum\" for all action: Action",

            // Logical negation is only over boolean types
            "!action == Actions::\"createAlbum\" for all action: Action",

            // +, -, * are only over numeric types
            "alice.age == (bob.name - 1)  for all alice: Account, bob: Account",
            "alice.age == (bob.name * 1)  for all alice: Account, bob: Account",
            "alice.age == (bob.name + 1)  for all alice: Account, bob: Account",

            // ==/!= can cannot be over records or sets
            "alice.friends == bob.friends for all alice: Account, bob: Account",
            "one.permission == another.permission for all one: PhotoPermission, another: PhotoPermission",

            // ==/!= should be over compatible types
            "one.code == another.index for all one: PhotoPermission, another: PhotoPermission",
            //"one == another for all one: Action, another: Photo",

            // && only over boolean types
            "alice.age && bob.name  for all alice: Account, bob: Account",
            "alice.age || bob.name  for all alice: Account, bob: Account",

            // Less/Greater operators only over boolean types
            "alice.age > bob.name for all alice: Account, bob: Account",
            "alice.age >= bob.name for all alice: Account, bob: Account",
            "alice.age < bob.name for all alice: Account, bob: Account",
            "alice.age <= bob.name for all alice: Account, bob: Account",

            // Quantifier: Ungrounded variable
            "actions == Action::\"createAlbum\" for all action: Action",

            // Quantifier: duplicate variable name
            "alice.age == alice.age for all alice: Account, alice: Account",

            // Quantifier: type does not exist
            "alice.age == alice.age for all alice: Accounts",

            // has: expects record-like types
            "alice.age has bar for all alice: Account",

            // is: expects valid record-like types
            "alice is Accounts for all alice: Account",
            "alice.age is Account for all alice: Account",

            // in: expects valid entity types
            "alice.age in Account::\"Alice\" for all alice: Account",
            //"alice in Account::\"Alice\" for all alice: Action", // ok
            //"alice in Account::\"Alice\" for all alice: Photoapp::Action", // ok
    })
    void fail(String text) {
        try {
            ok(text);
            throw new TranslationError("Unexpected test pass for invariant: " + text);
        } catch (InvariantValidation.Error error) {
            logger.info(MAGENTA, "    Expected error: " + error.getMessage());
        }
    }
}
