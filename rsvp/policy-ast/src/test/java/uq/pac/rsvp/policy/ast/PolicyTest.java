package uq.pac.rsvp.policy.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.Policy.Effect;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.StringExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression.BinaryOp;
import uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference;

@DisplayName("Policy AST")
public class PolicyTest {

    @Nested
    @DisplayName("JSON deserialisation")
    class TestJSONParsing {
        @Test
        @DisplayName("handles permit all")
        void testDeserialisation() {
            String json = "{ \"effect\": \"permit\", \"condition\": { \"type\": \"bool\", \"value\": \"true\" }}";
            Gson gson = new GsonBuilder().registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                    .create();
            Policy policy = gson.fromJson(json, Policy.class);
            assertTrue(policy.isPermit());
            assertFalse(policy.isForbid());
            assertEquals(SourceLoc.MISSING, policy.getSourceLoc());
            assertEquals("true", policy.getCondition().toString());
        }
    }

    @Nested
    @DisplayName("Manual construction")
    class TestManual {

        @Test
        @DisplayName("handles permit all")
        void permitAll() {
            Policy policy = new Policy(Effect.Permit, new BooleanExpression(true));
            assertTrue(policy.isPermit());
            assertFalse(policy.isForbid());
            assertEquals(SourceLoc.MISSING, policy.getSourceLoc());
            assertEquals("true", policy.getCondition().toString());
        }

        @Test
        @DisplayName("handles simple deny")
        void denyHacker() {
            Policy policy = new Policy(Effect.Forbid, new BinaryExpression(new VariableExpression(Reference.Principal),
                    BinaryOp.Eq, new StringExpression("hacker")));

            assertFalse(policy.isPermit());
            assertTrue(policy.isForbid());
            assertEquals(SourceLoc.MISSING, policy.getSourceLoc());
            assertEquals("(principal == \"hacker\")", policy.getCondition().toString());
        }

    }

}
