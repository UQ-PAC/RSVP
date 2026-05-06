package uq.pac.rsvp.policy.ast.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.PolicyAstNode;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing policy source locations.
 *
 */

public class PolicyLocationTest {

    /**
     * Assert the location has been set. By default, {@link SourceLoc#MISSING}
     * is used whenever a source location is not specified during construction.
     * Note that not all locations will be valid even if set.
     * In some cases, we cannot set source locations, for instance when combining
     * different clauses or if the clauses of a policy are not specified.
     */
    void assertLocation(FileSource fs, PolicyAstNode node) {
        SourceLoc loc = node.getSourceLoc();
        assertNotSame(SourceLoc.MISSING, loc);
        if (!loc.isEmpty()) {
            assertTrue(fs.isValid(node.getSourceLoc()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "euid.cedar",
            "amazon-k8s.cedar",
            "annotation.cedar",
            "contains.cedar",
            "empty.cedar",
            "healthcare.cedar",
            "is.cedar",
            "permit-all.cedar",
            "permit-and-forbid.cedar",
            "policy-with-has.cedar"
    })
    void testLocation(String file) throws IOException {
        URL url = ClassLoader.getSystemResource(file);
        Path path = Path.of(url.getPath());
        PolicyProgram program = PolicyProgram.parse(path);
        FileSource fs = new FileSource(path);
        program.getStatements().forEach(s -> testLocations(fs, s));
    }

    void testLocations(FileSource fs, PolicyAstNode node) {
        node.accept(new PolicyVisitor() {
            @Override
            public void visitPolicy(Policy policy) {
                assertLocation(fs, policy);
                // Policies that have no explicit condition are true
                // but have no assigned location
                if (!policy.getCondition().equals(new BooleanExpression(true))) {
                    policy.getCondition().accept(this);
                }
            }

            @Override
            public void visitInvariant(Invariant invariant) {
                assertLocation(fs, invariant);
                invariant.getQuantifier().accept(this);
                invariant.getExpression().accept(this);
            }

            @Override
            public void visitQuantifier(Quantifier quantifier) {
                // Variables and types have no locations for the moment
                // Perhaps can be reworked with variable expressions and type expressions
                // if needed
            }

            @Override
            public void visitBinaryExpr(BinaryExpression expr) {
                assertLocation(fs, expr);
                expr.getRight().accept(this);
                expr.getLeft().accept(this);
            }

            @Override
            public void visitCallExpr(CallExpression expr) {
                assertLocation(fs, expr);
                expr.getArgs().forEach(a -> a.accept(this));
                expr.getSelf().accept(this);
            }

            @Override
            public void visitConditionalExpr(ConditionalExpression expr) {
                assertLocation(fs, expr);
                expr.getCondition().accept(this);
                expr.getElse().accept(this);
                expr.getThen().accept(this);
            }

            @Override
            public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
                assertLocation(fs, expr);
                expr.getObject().accept(this);
            }

            @Override
            public void visitRecordExpr(RecordExpression expr) {
                assertLocation(fs, expr);
                expr.getProperties().values().forEach(v -> v.accept(this));
            }

            @Override
            public void visitSetExpr(SetExpression expr) {
                assertLocation(fs, expr);
                expr.getElements().forEach(e -> e.accept(this));
            }

            @Override
            public void visitUnaryExpr(UnaryExpression expr) {
                assertLocation(fs, expr);
                expr.getExpression().accept(this);
            }

            @Override
            public void visitVariableExpr(VariableExpression expr) {
                assertLocation(fs, expr);
            }

            @Override
            public void visitActionExpr(ActionExpression expr) {
                assertLocation(fs, expr);
            }

            @Override
            public void visitBooleanExpr(BooleanExpression expr) {
                assertLocation(fs, expr);
            }

            @Override
            public void visitEntityExpr(EntityExpression expr) {
                assertLocation(fs, expr);
            }

            @Override
            public void visitLongExpr(LongExpression expr) {
                assertLocation(fs, expr);
            }

            @Override
            public void visitSlotExpr(SlotExpression expr) {
                throw new AssertionError("Unsupported");
            }

            @Override
            public void visitStringExpr(StringExpression expr) {
                assertLocation(fs, expr);
            }

            @Override
            public void visitTypeExpr(TypeExpression expr) {
                assertLocation(fs, expr);
            }
        });
    }
}
