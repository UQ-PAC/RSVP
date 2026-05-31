package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;

public interface PolicyVisitor {
    default void visitPolicy(Policy policy) { throw new AssertionError(); }

    default void visitInvariant(Invariant invariant) { throw new AssertionError(); }

    default void visitQuantifier(Quantifier quantifier) { throw new AssertionError(); }

    default void visitBinaryExpr(BinaryExpression expr) { throw new AssertionError(); }

    default void visitCallExpr(CallExpression expr) { throw new AssertionError(); }

    default void visitConditionalExpr(ConditionalExpression expr) { throw new AssertionError(); }

    default void visitPropertyAccessExpr(PropertyAccessExpression expr) { throw new AssertionError(); }

    default void visitRecordExpr(RecordExpression expr) { throw new AssertionError(); }

    default void visitSetExpr(SetExpression expr) { throw new AssertionError(); }

    default void visitUnaryExpr(UnaryExpression expr) { throw new AssertionError(); }

    default void visitVariableExpr(VariableExpression expr) { throw new AssertionError(); }

    default void visitActionExpr(ActionExpression expr) { throw new AssertionError(); }

    default void visitBooleanExpr(BooleanExpression expr) { throw new AssertionError(); }

    default void visitEntityExpr(EntityExpression expr) { throw new AssertionError(); }

    default void visitLongExpr(LongExpression expr) { throw new AssertionError(); }

    default void visitStringExpr(StringExpression expr) { throw new AssertionError(); }

    default void visitTypeExpr(TypeExpression expr) { throw new AssertionError(); }

    default void visitHasExpr(HasExpression expr) { throw new AssertionError(); }

    default void visitIsExpr(IsExpression expr) { throw new AssertionError(); }
}
