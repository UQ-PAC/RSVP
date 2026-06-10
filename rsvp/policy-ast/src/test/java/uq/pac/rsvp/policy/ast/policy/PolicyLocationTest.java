package uq.pac.rsvp.policy.ast.policy;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.TestUtil;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing policy source locations.
 */
public class PolicyLocationTest {

    private final Path resourceDir =
            TestUtil.getResourceDir("policy", "location");

    void test(Path p) {
        try {
            String actual = Visitor.getData(p);
            Path expected = Path.of(p + ".expected");
            if (TestUtil.GENERATE_ORACLES) {
                Files.writeString(expected, actual);
            }
            assertEquals(Files.readString(expected), actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TestFactory
    List<DynamicTest> test() {
        return TestUtil.findFiles(resourceDir, ".cedar").stream().map(p -> {
            return DynamicTest.dynamicTest(p.getFileName().toString(), () -> test(p));
        }).toList();
    }

    private static class Visitor implements PolicyVisitor {
        private final FileSource fs;
        private final StringBuilder sb = new StringBuilder();
        private int indent = 0;

        private Visitor(FileSource fs) {
            this.fs = fs;
        }

        void log(String msg, AstNode node, Runnable consumer) {
            indent += 3;
            String locString = assertLocation(node);
            log("%s at %s".formatted(msg, locString));
            consumer.run();
            assertLocation(node);
            indent -= 3;
        }

        void log(String msg, AstNode node) {
            log(msg, node, () -> {});
        }

        void log(String msg) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.repeat(" ", Math.max(0, indent));
            sb.append(msg);
        }

        public static String getData(Path file) throws IOException {
            String filename = file.getFileName().toString();
            String text = Files.readString(file);
            FileSource fs = new FileSource(filename, text);

            Visitor visitor = new Visitor(fs);
            PolicyProgram program = PolicyProgram.parse(filename, text);
            program.getStatements().forEach(s -> s.accept(visitor));
            return visitor.sb.toString();
        }

        /**
         * In some cases, we cannot set source locations, for instance when combining
         * different clauses or if the clauses of a policy are not specified.
         */
        String assertLocation(AstNode node) {
            SourceLoc loc = node.getSourceLoc();
            // The location is either set or deliberately omitted
            assertTrue (!loc.isEmpty() || loc == PolicyStatementVisitor.OMITTED);
            // If the location is set then it has to be valid
            assertTrue (loc.isEmpty() || fs.isValid(loc));
            return loc == PolicyStatementVisitor.OMITTED ? "<omitted>" : loc.toString();
        }

        @Override
        public void visitPolicy(Policy policy) {
            log("Policy", policy, () -> {
                policy.getCondition().accept(this);
            });
        }

        @Override
        public void visitInvariant(Invariant invariant) {
            log("Invariant", invariant, () -> {
                invariant.getExpression().accept(this);
                invariant.getQuantifier().getVariables().forEach(v -> {
                    v.name().accept(this);
                    v.type().accept(this);
                });
            });
        }

        @Override
        public void visitBinaryExpr(BinaryExpression expr) {
            log("Bin: " + expr.toString(), expr, () -> {
                expr.getRight().accept(this);
                expr.getLeft().accept(this);
            });

        }

        @Override
        public void visitCallExpr(CallExpression expr) {
            log("Call:" + expr.toString(), expr, () -> {
                expr.getArgs().forEach(a -> a.accept(this));
                expr.getSelf().accept(this);
            });

        }

        @Override
        public void visitConditionalExpr(ConditionalExpression expr) {
            log("If: " + expr.toString(), expr, () -> {
                expr.getCondition().accept(this);
                expr.getElse().accept(this);
                expr.getThen().accept(this);
            });

        }

        @Override
        public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
            log("Prop: " + expr.toString(), expr, () -> {
                expr.getObject().accept(this);
            });
        }

        @Override
        public void visitRecordExpr(RecordExpression expr) {
            log("Record: " + expr.toString(), expr, () -> {
                expr.getProperties().values().forEach(v -> v.accept(this));
            });

        }

        @Override
        public void visitSetExpr(SetExpression expr) {
            log("Set: " + expr.toString(), expr, () -> {
                expr.getElements().forEach(e -> e.accept(this));
            });
        }

        @Override
        public void visitUnaryExpr(UnaryExpression expr) {
            log("Unary: " + expr.toString(), expr, () -> {
                expr.getExpression().accept(this);
            });
        }

        @Override
        public void visitVariableExpr(VariableExpression expr) {
            log("Var: " + expr.toString(), expr);
        }

        @Override
        public void visitActionExpr(ActionExpression expr) {
            log("Action: " + expr.toString(), expr);
        }

        @Override
        public void visitBooleanExpr(BooleanExpression expr) {
            log("Bool: " + expr.toString(), expr);
        }

        @Override
        public void visitEntityExpr(EntityExpression expr) {
            log("Entity: " + expr.toString(), expr);
        }

        @Override
        public void visitLongExpr(LongExpression expr) {
            log("Long: " + expr.toString(), expr);
        }

        @Override
        public void visitStringExpr(StringExpression expr) {
            log("Str: " + expr.toString(), expr);
        }

        @Override
        public void visitTypeExpr(TypeExpression expr) {
            log("Type: " + expr.toString(), expr);
        }

        @Override
        public void visitHasExpr(HasExpression expr) {
            log("Has: " + expr.toString(), expr, () -> {
                expr.getExpression().accept(this);
            });
        }

        @Override
        public void visitIsExpr(IsExpression expr) {
            log("Is: " + expr.toString(), expr, () -> {
                expr.getExpression().accept(this);
                expr.getTypeExpression().accept(this);
            });
        }
    }
}
