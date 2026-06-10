package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;

import static uq.pac.rsvp.policy.ast.AstNode.unsupported;

public interface PolicyComputationVisitor<T> {
    default T visitPolicy(Policy policy) { throw unsupported(policy); }

    default T visitInvariant(Invariant invariant) { throw unsupported(invariant); }

    default T visitBinaryExpr(BinaryExpression expr) { throw unsupported(expr); }

    default T visitCallExpr(CallExpression expr) { throw unsupported(expr); }

    default T visitConditionalExpr(ConditionalExpression expr) { throw unsupported(expr); }

    default T visitPropertyAccessExpr(PropertyAccessExpression expr) { throw unsupported(expr); }

    default T visitRecordExpr(RecordExpression expr) { throw unsupported(expr); }

    default T visitSetExpr(SetExpression expr) { throw unsupported(expr); }

    default T visitUnaryExpr(UnaryExpression expr) { throw unsupported(expr); }

    default T visitVariableExpr(VariableExpression expr) { throw unsupported(expr); }

    default T visitActionExpr(ActionExpression expr) { throw unsupported(expr); }

    default T visitBooleanExpr(BooleanExpression expr) { throw unsupported(expr); }

    default T visitEntityExpr(EntityExpression expr) { throw unsupported(expr); }

    default T visitLongExpr(LongExpression expr) { throw unsupported(expr); }

    default T visitStringExpr(StringExpression expr) { throw unsupported(expr); }

    default T visitTypeExpr(TypeExpression expr) { throw unsupported(expr); }

    default T visitHasExpr(HasExpression expr) { throw unsupported(expr); }

    default T visitIsExpr(IsExpression expr) { throw unsupported(expr); }
}
