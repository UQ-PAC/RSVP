package uq.pac.rsvp.cedar.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.cedar.ast.expr.Expression;
import uq.pac.rsvp.cedar.ast.expr.Expression.ExpressionDeserialiser;

public class PolicySetTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .create();
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "empty.ast.json;[]",
            "permit-all.ast.json;[permit on: true]",
            "permit-and-forbid.ast.json;[permit on: (principal == \"poppy\"), forbid on: (action == \"murder\")]"
    })
    void testDeserialisation(String file, String expected) throws IOException, URISyntaxException {
        URL url = ClassLoader.getSystemResource(file);
        String json = Files.readString(Path.of(url.toURI()));
        PolicySet policies = gson.fromJson(json, PolicySet.class);
        assertEquals(expected, policies.toString());
    }

}
