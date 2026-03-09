package uq.pac.rsvp.policy.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import uq.pac.rsvp.RsvpException;

@DisplayName("Policy set AST")
public class PolicySetTest {

    @Nested
    @DisplayName("Cedar policies")
    class TestCedarParsing {

        @ParameterizedTest
        @DisplayName("handles expressions")
        @CsvSource(delimiter = ';', value = {
                "permit-all.cedar;[permit on: true]",
                "permit-and-forbid.cedar;[permit on: (true && (true && (true && (principal == \"poppy\")))), forbid on: (true && (true && (true && (action == \"murder\"))))]",
                "euid.cedar;[permit on: ((principal is Account) && ((action == Action::\"viewPhoto\") && true))]"
        })
        void testCedarExpressionParsing(String file, String expected) throws RsvpException {
            URL url = ClassLoader.getSystemResource(file);
            PolicySet policies = PolicySet.parseCedarPolicySet(Path.of(url.getPath()));
            assertEquals(expected, policies.toString());
        }

        @Test
        @DisplayName("handles annotations")
        void testAnnotationParsing() throws RsvpException {
            URL url = ClassLoader.getSystemResource("annotation.cedar");
            PolicySet policies = PolicySet.parseCedarPolicySet(Path.of(url.getPath()));
            Policy policy = policies.getFirst();
            assertEquals(2, policy.getAnnotations().keySet().size());
            assertEquals("annotated!!", policy.getAnnotations().get("testing"));
            assertEquals("pointless annotation", policy.getAnnotations().get("another"));
        }
    }

    @Nested
    @DisplayName("JSON policies")
    class TestJSONParsing {

        @ParameterizedTest
        @DisplayName("handles expressions")
        @CsvSource(delimiter = ';', value = {
                "empty.ast.json;[]",
                "permit-all.ast.json;[permit on: true]",
                "permit-and-forbid.ast.json;[permit on: (principal == \"poppy\"), forbid on: (action == \"murder\")]"
        })
        void testDeserialisation(String file, String expected) throws IOException {
            URL url = ClassLoader.getSystemResource(file);
            String json = Files.readString(Path.of(url.getPath()));
            PolicySet policies = JsonParser.parsePolicySet(json);
            assertEquals(expected, policies.toString());
        }
    }

}
