package uq.pac.rsvp.policy.ast.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.expr.EntityExpression.EntityExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;

public class ExpressionTest {
    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .registerTypeAdapter(EntityExpression.class, new EntityExpressionDeserialiser())
                .create();
    }

    @Test
    void testDeserialisation() throws IOException, URISyntaxException {
        URL url = ClassLoader.getSystemResource("expr.ast.json");
        String json = Files.readString(Path.of(url.toURI()));
        PolicySet policies = gson.fromJson(json, PolicySet.class);

        String[] expected = {
                "((principal.role == \"normie\") && ([\"secret\", \"top secret\"].contains(resource.access) || (resource has \"leaked\")))",
                "(((2 + 2) == (7 * 9)) || ((3 - 5) != -2))",
                "(if !(action == Gardening::\"waterRoses\"); then (principal.hobby like \"g*rdening\"); else (principal != &principal))",
                "({ key: \"value\" }.key == { \"k e y\": \"value\" }[\"k e y\"])"
        };

        int i = 0;
        for (Policy policy : policies) {
            Expression condition = policy.getCondition();

            // Check source loc
            SourceLoc source = condition.getSourceLoc();
            if (i == 0) {
                assertEquals("expr.cedar", source.file);
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
    void testInvalidAstFile() throws IOException, URISyntaxException {
        URL url = ClassLoader.getSystemResource("invalid.ast.json");
        String json = Files.readString(Path.of(url.toURI()));
        assertThrows(JsonParseException.class, new Executable() {
            @Override
            public void execute() {
                gson.fromJson(json, PolicySet.class);
            }
        });
    }
}
