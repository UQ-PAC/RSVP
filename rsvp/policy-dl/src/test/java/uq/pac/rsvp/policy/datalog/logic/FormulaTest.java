package uq.pac.rsvp.policy.datalog.logic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.AssertionFailedError;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormulaTest {

    static class FormulaParser implements PolicyComputationVisitor<Formula> {

        private FormulaParser() {}

        private static final FormulaParser VISITOR = new FormulaParser();

        @Override
        public Formula visitBinaryExpr(BinaryExpression expr) {
            Formula lhs = expr.getLeft().compute(this);
            Formula rhs = expr.getRight().compute(this);
            return switch (expr.getOperator()) {
                case Or -> new Disjunction(lhs, rhs);
                case And -> new Conjunction(lhs, rhs);
                default -> throw new AssertionFailedError("Unexpected");
            };
        }

        @Override
        public Formula visitUnaryExpr(UnaryExpression expr) {
            if (expr.getOperator() == UnaryExpression.Operator.Not) {
                return new Negation(expr.getExpression().compute(this));
            }
            throw new AssertionFailedError("Unexpected");
        }

        @Override
        public Formula visitVariableExpr(VariableExpression expr) {
            return new Predicate<>(expr.getReference());
        }

        @Override
        public Formula visitBooleanExpr(BooleanExpression expr) {
            return expr.getValue() ? Literal.TRUE : Literal.FALSE;
        }

        public static Formula parse(String formula) {
            return Expression.parse(formula).compute(VISITOR);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "(a || b) && c, ((a && c) || (b && c))",
            "a && (b || c), ((a && b) || (a && c))",
            "(a || b) && (c || d), (((a && c) || (a && d)) || ((b && c) || (b && d)))",
            "(a || b || c) && d, (((a && d) || (b && d)) || (c && d))",
            "(a && (b && (c || d))), ((a && (b && c)) || (a && (b && d)))",
    })
    void dnf(String input, String expected) {
        Formula f = FormulaParser.parse(input);
        Formula dnf = DNFTransformer.transform(f);
        assertEquals(expected, dnf.toString());
    }
}
