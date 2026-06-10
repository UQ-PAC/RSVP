package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.Invariant;

import static uq.pac.rsvp.policy.ast.AstNode.unsupported;

public interface PolicyVisitor {
    default void visitPolicy(Policy policy) { throw unsupported(policy); }

    default void visitInvariant(Invariant invariant) { throw unsupported(invariant); }

    default void visitBinaryExpr(BinaryExpression expr) { throw unsupported(expr); }

    default void visitCallExpr(CallExpression expr) { throw unsupported(expr); }

    default void visitConditionalExpr(ConditionalExpression expr) { throw unsupported(expr); }

    default void visitPropertyAccessExpr(PropertyAccessExpression expr) { throw unsupported(expr); }

    default void visitRecordExpr(RecordExpression expr) { throw unsupported(expr); }

    default void visitSetExpr(SetExpression expr) { throw unsupported(expr); }

    default void visitUnaryExpr(UnaryExpression expr) { throw unsupported(expr); }

    default void visitVariableExpr(VariableExpression expr) { throw unsupported(expr); }

    default void visitActionExpr(ActionExpression expr) { throw unsupported(expr); }

    default void visitBooleanExpr(BooleanExpression expr) { throw unsupported(expr); }

    default void visitEntityExpr(EntityExpression expr) { throw unsupported(expr); }

    default void visitLongExpr(LongExpression expr) { throw unsupported(expr); }

    default void visitStringExpr(StringExpression expr) { throw unsupported(expr); }

    default void visitTypeExpr(TypeExpression expr) { throw unsupported(expr); }

    default void visitHasExpr(HasExpression expr) { throw unsupported(expr); }

    default void visitIsExpr(IsExpression expr) { throw unsupported(expr); }
}
