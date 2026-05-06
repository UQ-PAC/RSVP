package uq.pac.rsvp.policy.ast.policy.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uq.pac.rsvp.policy.ast.policy.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.policy.expr.VariableExpression;


@DisplayName("Property Access Expression")
public class ProperyAccessExpressionTest {

    @ParameterizedTest
    @DisplayName("Renders property accesses correctly")
    @CsvSource({ "accessLevel,principal.accessLevel", "prop,principal.prop", "wacky prop,principal[\"wacky prop\"]",
            "under_score,principal.under_score", "f$*#,principal[\"f$*#\"]" })
    public void testRender(String propName, String expected) {
        VariableExpression principal = new VariableExpression("principal");
        assertEquals(expected, new PropertyAccessExpression(principal, propName).toString());
    }
}
