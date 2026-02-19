package uq.pac.rsvp.policy.datalog;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.translation.TranslationDriver;

import java.io.IOException;
import java.nio.file.Path;

public class TranslationDriverTest {

    private static final String ENTITTES =
            "examples/photoapp/photoapp-entities-namespaced.json";

    @Test
    public void test() throws IOException, AuthException {
        Entities entities = Entities.parse(Path.of(ENTITTES));
        DLProgram program = TranslationDriver.getTranslation(entities);
        System.out.println(program);
    }
}
