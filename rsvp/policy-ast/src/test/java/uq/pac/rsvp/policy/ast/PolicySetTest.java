package uq.pac.rsvp.policy.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitorImpl;

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

        @Test
        @DisplayName("handles 'has' in policy conditions")
        void testHasParsing() throws RsvpException {
            URL url = ClassLoader.getSystemResource("policy-with-has.cedar");
            PolicySet policies = PolicySet.parseCedarPolicySet(Path.of(url.getPath()));
            Policy policy = policies.getFirst();
            int stage[] = new int[] { 0 };
            PolicyVisitor pv = new PolicyVisitorImpl() {
                @Override
                public void visitBinaryExpr(BinaryExpression expr) {
                    // First we will see '&&' and then the 'has', i.e.:
                    //
                    // (resource has owner) && ...
                    // | |
                    // | stage 0
                    // stage 1
                    //
                    // But we actually see a bunch of '&&' because the (tautological) conditions
                    // from (resource, owner, principal) are apparently included.
                    if (stage[0] == 0) {
                        assertEquals(expr.getOp(), BinaryExpression.BinaryOp.And);
                        stage[0] = 1;
                    } else if (stage[0] == 1) {
                        if (expr.getOp() == BinaryExpression.BinaryOp.HasAttr) {
                            assertEquals(44, expr.getSourceLoc().offset);
                            assertEquals(18, expr.getSourceLoc().len);
                            stage[0] = 2;
                        } else {
                            assertEquals(expr.getOp(), BinaryExpression.BinaryOp.And);
                        }
                    }
                    super.visitBinaryExpr(expr);
                }
            };
            policy.getCondition().accept(pv);
            assertEquals(2, stage[0]);
        }

        @Test
        @DisplayName("handles contains")
        void testContains() throws RsvpException {
            URL url = ClassLoader.getSystemResource("contains.cedar");
            PolicySet policies = PolicySet.parseCedarPolicySet(Path.of(url.getPath()));
            policies.accept(new PolicyVisitorImpl() {
                @Override
                public void visitCallExpr(CallExpression expr) {

                    super.visitCallExpr(expr);
                    assertTrue(expr.getSelf() instanceof PropertyAccessExpression);
                    assertEquals("contains", expr.getFunc());
                    assertTrue(expr.getArgs().getFirst() instanceof VariableExpression);
                }
            });
        }

        @Test
        @DisplayName("handles source locations of argument conditions")
        void testArgConditions() throws RsvpException {
            String policy = "permit (\n" + //
                    "    principal in Group::\"cool group\",\n" + //
                    "    action == Action::\"coolAction\",\n" + //
                    "    resource\n" + //
                    ");";

            PolicySet policies = PolicySet.parseCedarPolicySet("test.cedar", policy);

            policies.accept(new PolicyVisitorImpl() {
                @Override
                public void visitBinaryExpr(BinaryExpression expr) {
                    System.err.println(expr.toString());
                    System.err.println(expr.getSourceLoc().toString());
                    expr.getLeft().accept(this);
                    expr.getRight().accept(this);
                }
            });

            policy = "permit (principal, action, resource);";
            policies = PolicySet.parseCedarPolicySet("test.cedar", policy);

            // FIXME: source locations missing
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
            PolicySet policies = JsonParser.parsePolicySet("file.json", json, "");
            assertEquals(expected, policies.toString());
        }
    }

}
