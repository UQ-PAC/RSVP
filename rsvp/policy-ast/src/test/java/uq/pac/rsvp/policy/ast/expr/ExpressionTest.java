package uq.pac.rsvp.policy.ast.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParseException;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.deserialisation.PolicyJsonParser;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.support.SourceLoc;

@DisplayName("Policy condition AST")
public class ExpressionTest {
    @Nested
    @DisplayName("Cedar parsing")
    class TestCedarParsing {
        @Test
        @DisplayName("handles is expressions")
        void testTypeNode() throws RsvpException {
            URL url = ClassLoader.getSystemResource("is.cedar");
            PolicySet policies = PolicySet.parseCedarPolicySet(Path.of(url.getPath()));

            Expression condition = policies.getFirst().getCondition();
            assertTrue(condition instanceof BinaryExpression);
            assertTrue(((BinaryExpression) condition).getLeft() instanceof BinaryExpression);

            BinaryExpression is = (BinaryExpression) (((BinaryExpression) condition).getLeft());
            assertEquals(BinaryExpression.BinaryOp.Is, is.getOp());

            assertTrue(is.getLeft() instanceof VariableExpression);
            assertTrue(is.getRight() instanceof TypeExpression);

            assertEquals("App::Role::Admin", ((TypeExpression) (is.getRight())).getValue());
        }
    }

    @Nested
    @DisplayName("JSON deserialisation")
    class TestJSONParsing {

        @Test
        @DisplayName("handles basic expressions")
        void testDeserialisation() throws IOException, URISyntaxException {
            URL url = ClassLoader.getSystemResource("expr.ast.json");
            String json = Files.readString(Path.of(url.toURI()));
            PolicySet policies = PolicyJsonParser.parsePolicySet("file.json", json,
                    Files.readString(Path.of(url.getPath())));

            String[] expected = {
                    "((principal.role == \"normie\") && ([\"secret\", \"top secret\"].contains(resource.access) || (resource has \"leaked\")))",
                    "(((2 + 2) == (7 * 9)) || ((3 - 5) != -2))",
                    "(if !(action == Gardening::\"waterRoses\"); then (principal.hobby like \"g*rdening\"); else (principal != &principal))",
                    "({ key: \"value\" }.key == { \"k e y\": \"value\" }[\"k e y\"])"
            };

            int i = 0;
            for (Policy policy : policies.getPolicies()) {
                Expression condition = policy.getCondition();

                // Check source loc
                SourceLoc source = condition.getSourceLoc();
                if (i == 0) {
                    assertEquals("file.json", source.file);
                    assertEquals(5, source.offset);
                    assertEquals(25, source.len);
                } else {
                    assertEquals(SourceLoc.MISSING, source);
                }

                // Check expression
                assertEquals(expected[i], condition.toString());
                i++;
            }
        }

        @Test
        @DisplayName("handles invalid types")
        void testInvalidAstFile() throws IOException, URISyntaxException {
            URL url = ClassLoader.getSystemResource("invalid.ast.json");
            String json = Files.readString(Path.of(url.toURI()));
            assertThrows(JsonParseException.class, () -> PolicyJsonParser.parsePolicySet("file.json", json));
        }

        @Test
        @DisplayName("handles is expressions")
        void testTypeNode() throws IOException, URISyntaxException {
            URL url = ClassLoader.getSystemResource("is.ast.json");
            String json = Files.readString(Path.of(url.toURI()));

            PolicySet policies = PolicyJsonParser.parsePolicySet("file.json", json);

            Expression condition = policies.getFirst().getCondition();
            assertTrue(condition instanceof BinaryExpression);

            BinaryExpression is = (BinaryExpression) condition;
            assertEquals(BinaryExpression.BinaryOp.Is, is.getOp());

            assertTrue(is.getLeft() instanceof VariableExpression);
            assertTrue(is.getRight() instanceof TypeExpression);

            assertEquals("App::Role::Admin", ((TypeExpression) (is.getRight())).getValue());
        }
    }
}
