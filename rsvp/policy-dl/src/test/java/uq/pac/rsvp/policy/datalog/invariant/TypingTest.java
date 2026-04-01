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
import static org.junit.jupiter.api.Assertions.fail;

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

            // Function validation

            // unregistered function
            "no: foobar(p, r, a) for all a: Action, p: Account, r: Photo",

            // allow
            "ok: allow(p, r, a) for all a: Action, p: Account, r: Photo",
            "ok: allow(p, r, Action::\"viewPhoto\") for all p: Account, r: Photo",
            "ok: allow(Account::\"Alice\", r, a) for all a: Action, r: Photo",
            "ok: allow(p, Photo::\"AlicePassport\", a) for all a: Action, p: Account, r: Photo",
            "no: allow(a, r, a) for all a: Action, p: Account, r: Photo",
            "no: allow(1, r, a) for all a: Action, p: Account, r: Photo",
            "no: allow(p, a, a) for all a: Action, p: Account, r: Photo",
            "no: allow(p, r, r) for all a: Action, p: Account, r: Photo",
            "no: p.allow(p, r, r) for all a: Action, p: Account, r: Photo",
            "no: allow(p, r) for all a: Action, p: Account, r: Photo",
            "no: allow() for all a: Action, p: Account, r: Photo",

            // Deny
            "ok: deny(p, r, a) for all a: Action, p: Account, r: Photo",
            "ok: deny(p, r, Action::\"viewPhoto\") for all p: Account, r: Photo",
            "ok: deny(Account::\"Alice\", r, a) for all a: Action, r: Photo",
            "ok: deny(p, Photo::\"AlicePassport\", a) for all a: Action, p: Account, r: Photo",
            "no: deny(a, r, a) for all a: Action, p: Account, r: Photo",
            "no: deny(1, r, a) for all a: Action, p: Account, r: Photo",
            "no: deny(p, a, a) for all a: Action, p: Account, r: Photo",
            "no: deny(p, r, r) for all a: Action, p: Account, r: Photo",
            "no: p.deny(p, r, r) for all a: Action, p: Account, r: Photo",
            "no: deny(p, r) for all a: Action, p: Account, r: Photo",
            "no: deny() for all a: Action, p: Account, r: Photo",

            // isEmpty
            "ok: a.friends.isEmpty() for all a: Account",
            "ok: a.contents.strings.isEmpty() for all a: Container",
            "no: a.friends.isEmpty(1) for all a: Account",
            "no: isEmpty() for all a: Account",
            "no: a.role.isEmpty() for all a: Account",

            // contains
            "ok: a.friends.contains(b) for all a: Account, b: Account",
            "ok: a.friends.contains(b.role) for all a: Account, b: Account",
            "ok: c.contents.booleans.contains(true) for all c: Container",
            "ok: c.contents.numbers.contains(1) for all c: Container",
            "ok: c.contents.numbers.contains(1) for all c: Container",
            // allow entities/actions to be compared
            "ok: a.friends.contains(a.role) for all a: Account",
            "ok: a.friends.contains(Action::\"viewPhoto\") for all a: Account",
            "no: a.friends.contains(1) for all a: Account",
            "no: c.contents.numbers.contains(false) for all c: Container",
            "no: c.contents.numbers.contains(\"a\") for all c: Container",
            "no: contains(\"a\") for all c: Container",
            "no: a.friends.contains(a.role, a.role) for all a: Account",
            "no: a.friends.contains() for all a: Account",

    })
    void typeTest(String invariantText) {
        boolean pass = true;
        if (invariantText.startsWith("ok: ")) {
            pass = true;
        } else if (invariantText.startsWith("no: ")) {
            pass = false;
        } else {
            fail("Invalid prefix: " + invariantText);
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
