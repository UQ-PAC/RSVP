package uq.pac.rsvp.policy.ast.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.Expression.ExpressionDeserialiser;
import uq.pac.rsvp.policy.ast.expr.LongExpression;
import uq.pac.rsvp.policy.ast.expr.SlotExpression;
import uq.pac.rsvp.policy.ast.expr.StringExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;

public class PolicyVisitorImplTest {

    static Gson gson;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder().registerTypeAdapter(Expression.class, new ExpressionDeserialiser())
                .create();
    }

    static class TestVisitor extends PolicyVisitorImpl {
        public int vars = 0, bools = 0, entities = 0, longs = 0, slots = 0, strings = 0;

        @Override
        public void visitVariableExpr(VariableExpression expr) {
            vars++;
        }

        @Override
        public void visitBooleanExpr(BooleanExpression expr) {
            bools++;
        }

        @Override
        public void visitEntityExpr(EntityExpression expr) {
            entities++;
        }

        @Override
        public void visitLongExpr(LongExpression expr) {
            longs++;
        }

        @Override
        public void visitSlotExpr(SlotExpression expr) {
            slots++;
        }

        @Override
        public void visitStringExpr(StringExpression expr) {
            strings++;
        }
    }

    @Test
    void visitsParsedAst() throws IOException, URISyntaxException {
        URL url = ClassLoader.getSystemResource("expr.ast.json");
        String json = Files.readString(Path.of(url.toURI()));
        PolicySet policies = gson.fromJson(json, PolicySet.class);

        TestVisitor visitor = new TestVisitor();

        visitor.visitPolicySet(policies);
        assertEquals(6, visitor.vars);
        assertEquals(0, visitor.bools);
        assertEquals(1, visitor.entities);
        assertEquals(7, visitor.longs);
        assertEquals(1, visitor.slots);
        assertEquals(7, visitor.strings);
    }
}
