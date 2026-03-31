package uq.pac.rsvp.policy.datalog.invariant;

import com.cedarpolicy.model.entity.Entities;
import org.fusesource.jansi.Ansi;
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

import static org.fusesource.jansi.Ansi.Color.*;

public class TypingTest {
    Logger logger = new Logger();

    private final InvariantValidator validator;

    public TypingTest() throws RsvpException, IOException {
        Path schemaPath = TestUtil.getResourceDir("translation", "photoapp", "photoapp.cedarschema");
        Schema schema = Schema.parseCedarSchema(schemaPath);
        Path entitiesPath = TestUtil.getResourceDir("translation", "photoapp", "entities.json");
        Entities entities = Translation.updateEntities(Entities.parse(entitiesPath), schema);
        this.validator = new InvariantValidator(schema, entities);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Standalone expressions are expected to evaluate to booleans
            "ok: true",
            "ok: false",
            "ok: false && true",
            "ok: false || true",
            "ok: !false || true",
            "ok: !(false || true)",
            "ok: !(false || true)",
            "no: 1",
            "no: 1 + 2",
            "no: -(1 + 2)",
            "no: \"foo\"",
            "no: principal.age for all principal: Account",
            "no: Account::\"Alice\"",

            // Invalid entity name (type reference)
            "ok: principal == Account::\"Alice\" for all principal: Account",
            "no: principal == Accounts::\"Alice\" for all principal: Account",
            "no: principal == Account::\"Alic\" for all principal: Account",

            // Invalid action name (type reference)
            "ok: action == Action::\"createAlbum\" for all action: Action",
            "no: action == Action::\"createAlbu\" for all action: Action",
            "no: action == Actions::\"createAlbum\" for all action: Action",

            // Invalid property access
            "ok: permission.permission.read for all permission: PhotoPermission",
            "ok: guest.friend.role == Role::\"User\" for all guest: Guest",
            "no: one.codes == another.code for all one: PhotoPermission, another: PhotoPermission",
            "no: one.permissions.read == another.permission.read for all one: PhotoPermission, another: PhotoPermission",
            "no: one.permission.reads == another.permission.read for all one: PhotoPermission, another: PhotoPermission",

            // Arithmetic negation is only over numeric types
            "ok: -alice.age == -1 for all alice: Account",
            "no: -action == Actions::\"createAlbum\" for all action: Action",

            "ok: !true || !false for all action: Action",
            "ok: !(action == Action::\"createAlbum\") for all action: Action",
            "no: !action == Action::\"createAlbum\" for all action: Action",
            "no: !alice.age == 3 for all alice: Account",

            // +, -, * are only over numeric types
            "ok: 1 + 2 == 3",
            "ok: alice.age == (bob.age - 1)  for all alice: Account, bob: Account",
            "ok: alice.age + bob.age * alice.age == 3 for all alice: Account, bob: Account",
            "no: alice.age == (bob.name - 1)  for all alice: Account, bob: Account",
            "no: alice.age == (bob.name * 1)  for all alice: Account, bob: Account",
            "no: alice.age == (bob.name + 1)  for all alice: Account, bob: Account",

            // ==|!= over Long, String, Bool or Entity is ok.
            // but it cannot be over records or sets
            "ok: alice == bob for all alice: Account, bob: Role",
            "ok: alice == bob for all alice: Action, bob: Action",
            "ok: alice.age == bob.age for all alice: Account, bob: Account",
            "ok: alice.age == bob.age for all alice: Account, bob: Account",
            "ok: one.code == another.code for all one: PhotoPermission, another: PhotoPermission",
            "ok: one.permission.read == another.permission.write for all one: PhotoPermission, another: PhotoPermission",
            "ok: one == another for all one: Action, another: Photo",
            // but it cannot be over records or sets
            "no: alice.friends == bob.friends for all alice: Account, bob: Account",
            "no: one.permission == another.permission for all one: PhotoPermission, another: PhotoPermission",
            // ==/!= should be over compatible types
            "no: one.code == another.index for all one: PhotoPermission, another: PhotoPermission",

            // && only over boolean types
            "ok: one.permission.read && another.permission.write for all one: PhotoPermission, another: PhotoPermission",
            "ok: one.permission.read || another.permission.write for all one: PhotoPermission, another: PhotoPermission",
            "no: alice.age && bob.name  for all alice: Account, bob: Account",
            "no: alice.age || bob.name  for all alice: Account, bob: Account",

            // Less/Greater operators only over boolean types
            "ok: alice.age > bob.size for all alice: Account, bob: Photo",
            "ok: alice.age >= bob.size for all alice: Account, bob: Photo",
            "ok: alice.age < bob.size for all alice: Account, bob: Photo",
            "ok: alice.age <= bob.size for all alice: Account, bob: Photo",
            "no: alice.age > bob.name for all alice: Account, bob: Account",
            "no: alice.age >= bob.name for all alice: Account, bob: Account",
            "no: alice.age < bob.name for all alice: Account, bob: Account",
            "no: alice.age <= bob.name for all alice: Account, bob: Account",

            // Quantifier: Ungrounded variable
            "no: actions == Action::\"createAlbum\" for all action: Action",

            // Quantifier: duplicate variable name
            "no: alice.age == alice.age for all alice: Account, alice: Account",

            // Quantifier: type does not exist
            "no: alice.age == alice.age for all alice: Accounts",

            // has: expects record-like types
            "ok: alice has bar for all alice: Action",
            "ok: alice has bar for all alice: Account",
            "ok: alice.role has bar for all alice: Account",
            "ok: alice has \"bar\" for all alice: Action",
            "ok: alice has \"bar\" for all alice: Account",
            "ok: alice.role has \"bar\" for all alice: Account",
            "ok: alice.permission has bar for all alice: PhotoPermission",
            "no: alice.age has bar for all alice: Account",
            "no: alice.age has \"bar\" for all alice: Account",

            // is: expects valid record-like types
            "ok: alice is Photo for all alice: Account",
            "no: alice is Accounts for all alice: Account",
            "no: alice.age is Account for all alice: Account",

            // in: expects valid entity types
            "ok: alice in Account::\"Alice\" for all alice: Action",
            "ok: alice in Account::\"Alice\" for all alice: Photoapp::Action",
            "no: alice.age in Account::\"Alice\" for all alice: Account",
    })
    void typeTest(String invariantText) {
        boolean pass = true;
        if (invariantText.startsWith("ok: ")) {
            pass = true;
        } else if (invariantText.startsWith("no: ")) {
            pass = false;
        } else {
            typeTest("Invalid prefix: " + invariantText);
        }

        invariantText = invariantText.substring(4);

        try {
            String text = """
                @invariant("test") %s;
                """.formatted(invariantText);
            Ansi.Color colour = pass ? GREEN : YELLOW;
            logger.info(colour, "[*] %s", invariantText);
            validator.validate(InvariantSet.parse(text).stream().findAny().orElseThrow());
            if (!pass) {
                throw new TranslationError("Unexpected test pass for invariant: " + invariantText);
            }

        } catch (InvariantValidator.Error error) {
            if (!pass) {
                logger.info(MAGENTA, "    Expected error: " + error.getMessage());
            } else {
                throw error;
            }
        }
    }
}
